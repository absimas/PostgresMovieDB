package com.simas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Created by Simas Abramovas on 2015 Apr 02.
 */

public class MoviesFrames extends BaseFrame implements TableModelListener {

	private static final String MOVIE_QUERY_POSTFIX = "poster";
	private List<ThumbedMovie> mThumbedMovies = new ArrayList<>();
	private static final String[] COLUMN_NAMES = {"Poster", "Name", "Year", "Rating", "Votes"};
	private static final String OPTIONS_TITLE = "Options";
	private static final String SHOW_ACTORS_TITLE = "Show movie actors";
	private static final String DELETE_TITLE = "Delete movie";
	private static final String ADD = "Add";
	private static final String SEARCH = "Search";
	private static final int INITIAL_MOVIE_COUNT = 1;

	/**
	 * 0 = Unsorted, -1 = descending, 1 = ascending
	 */
	private int mOrder = 0;
	/**
	 * Column index by which the rows are ordered
	 */
	private int mOrderByCol;
	private JPopupMenu mRightClickMenu;

	private JTable mTable;

	public MoviesFrames() {
		new Thread(() -> {
			populateTable(null);
		}).start();
		setVisible(true);
	}

	private class ThumbedMovie {
		DB.Movie movie;
		ImageIcon thumb;
	}

	@Override
	public void addComponents() {
		getContentPane().setLayout(new BorderLayout());

		/* Top Panel (new movie + search) */
		JButton addButton = new JButton(ADD);
		addButton.addActionListener(e -> new NewMovieDialog(MoviesFrames.this).setVisible(true));
		JPanel searchPanel = new JPanel(new FlowLayout());
		JTextField searchField = new JTextField(40);
		JButton searchButton = new JButton(SEARCH);
		searchField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// Call search button if enter was pressed while on the JTextField
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchButton.doClick();
				}
			}
		});
		searchButton.addActionListener(e -> {
			if (searchField.getText().length() > 0) {
				populateTable(searchField.getText());
			}
		});
		searchPanel.add(searchField);
		searchPanel.add(searchButton);

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(addButton, BorderLayout.WEST);
		topPanel.add(searchPanel, BorderLayout.EAST);
		getContentPane().add(topPanel, BorderLayout.PAGE_START);
		/**********************************/

		mTable = new JTable() {
			//  Returning the Class of each column will allow different renderers to be used
			public Class getColumnClass(int column) {
				Object obj = getValueAt(0, column);
				return (obj != null) ? obj.getClass() : super.getColumnClass(column);
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				return column != 0 && super.isCellEditable(row, column);
			}
		};
		customizeTable();
		JScrollPane scrollPane = new JScrollPane(mTable);
		// Make scrolling slower
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
	}

	private void customizeTable() {
		// Header click listener
		mTable.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int col = mTable.columnAtPoint(e.getPoint());
				if (col != 0) {
					// Determine the order and column
					if (mOrder == 0 || mOrderByCol != col) {
						mOrder = 1;
						mOrderByCol = col;
					} else {
						mOrder *= -1;
					}
					toggleOrder(col);
					// Convert to 2d array
					Object[][] data = new Object[mThumbedMovies.size()][];
					int i = 0;
					for (ThumbedMovie tm : mThumbedMovies) {
						data[i++] = new Object[]{tm.thumb, tm.movie.name, tm.movie.year,
								tm.movie.rating, tm.movie.votes};
					}
					((DefaultTableModel) mTable.getModel()).setDataVector(data, COLUMN_NAMES);
				}
			}
		});

		/* Right click menu */
		mRightClickMenu = new JPopupMenu(OPTIONS_TITLE);
		// Menu items
		JMenuItem showActors = new JMenuItem(SHOW_ACTORS_TITLE);
		showActors.addActionListener(e -> {
			int row = mTable.getSelectedRow();
			new ActorsFrame(mThumbedMovies.get(row).movie).setVisible(true);
		});
		mRightClickMenu.add(showActors);
		JMenuItem deleteRow = new JMenuItem(DELETE_TITLE);
		deleteRow.addActionListener(e -> {
			int row = mTable.getSelectedRow();

			if (sDB.deleteMovie(mThumbedMovies.get(row).movie)) {
				// Removal from DB was successful now remove the row
				((DefaultTableModel)mTable.getModel()).removeRow(row);
				System.out.println("Delete succesful!");
			} else {
				// Removal from DB failed...
				System.err.println("Delete failed!");
			}
		});
		mRightClickMenu.add(deleteRow);
		/* ---------------- */

		// Mouse listener selects items and shows right click menu
		mTable.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// Select row
				int row = mTable.rowAtPoint(e.getPoint());
				mTable.setRowSelectionInterval(row, row);

				// If right click, show menu
				if (e.getButton() == MouseEvent.BUTTON3) {
					mRightClickMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}
		});

		// Row height
		mTable.setRowHeight(ThumbFetcher.MAX_THUMB_SIZE.width + 10);
		// Terminate field edit when focus has been lost
		mTable.putClientProperty("terminateEditOnFocusLost", true);
		// Only a single item may be selected at a time
		mTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// Center strings and ints
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		mTable.setDefaultRenderer(String.class, centerRenderer);
		mTable.setDefaultRenderer(Integer.class, centerRenderer);
		// Center doubles
		DefaultTableCellRenderer doubleRenderer = (DefaultTableCellRenderer)
				mTable.getDefaultRenderer(Double.class);
		doubleRenderer.setHorizontalAlignment(SwingConstants.CENTER);
		mTable.setDefaultRenderer(Double.class, doubleRenderer);
		// Grid color
		mTable.setGridColor(Color.decode("#EBEBEB"));
		// Disable column re-arranging
		mTable.getTableHeader().setReorderingAllowed(false);
	}

	private void toggleOrder(int index) {
		switch (index) {
			case 1:
				Collections.sort(mThumbedMovies,
						(o1, o2) -> o1.movie.name.compareToIgnoreCase(o2.movie.name) * mOrder);
				break;
			case 2:
				Collections.sort(mThumbedMovies,
						(o1, o2) -> (o2.movie.year - o1.movie.year) * mOrder);
				break;
			case 3:
				Collections.sort(mThumbedMovies,
						(o1, o2) -> (int) (o2.movie.rating - o1.movie.rating) * mOrder);
				break;
			case 4:
				Collections.sort(mThumbedMovies,
						(o1, o2) -> (o2.movie.votes - o1.movie.votes) * mOrder);
				break;
			default:
				System.err.println("Unrecognized column index to sort! " + index);
				break;
		}
	}

	public void populateTable(String query) {
		List<DB.Movie> movies;
		if (query == null || query.length() == 0) {
			movies = sDB.selectMovies(INITIAL_MOVIE_COUNT, 0);
		} else {
			movies = sDB.findMovies(query);
		}

		mThumbedMovies = new ArrayList<>();
		DefaultTableModel model = new DefaultTableModel();

		int i = 0;
		for (DB.Movie movie : movies) {
			ThumbedMovie tm = new ThumbedMovie();
			tm.movie = movie;
			final int row = i++;
			tm.thumb = ThumbFetcher.getImageIcon(movie.name + " " + MOVIE_QUERY_POSTFIX,
					() -> model.fireTableCellUpdated(row, 0));
			mThumbedMovies.add(tm);
		}

		// Convert to 2d array
		Object[][] data = new Object[mThumbedMovies.size()][];
		i=0;
		for (ThumbedMovie tm : mThumbedMovies) {
			data[i++] = new Object[] {tm.thumb, tm.movie.name, tm.movie.year, tm.movie.rating,
					tm.movie.votes};
		}
		model.setDataVector(data, COLUMN_NAMES);
		SwingUtilities.invokeLater(() -> mTable.setModel(model));
		model.addTableModelListener(this);
	}


	@Override
	public void tableChanged(TableModelEvent e) {
		// Ignore uneditable columns
		if (e.getColumn() <= 0) return;

		int row = e.getFirstRow();
		ThumbedMovie thumbedMovie = mThumbedMovies.get(row);

		switch (e.getColumn()) {
			case 1:
				String name = (String) mTable.getModel().getValueAt(row, 1);
				// If new value is empty, reset it
				if (name == null || name.length() == 0) {
					mTable.getModel().setValueAt(thumbedMovie.movie.name, row, 1);
					return;
				} else {
					thumbedMovie.movie.name = name;
				}
				break;
			case 2:
				Integer year = (Integer) mTable.getModel().getValueAt(row, 2);
				// If new value null, reset it
				if (year == null) {
					mTable.getModel().setValueAt(thumbedMovie.movie.year, row, 2);
					return;
				} else {
					thumbedMovie.movie.year = year;
				}
				break;
			case 3:
				Double rating = (Double) mTable.getModel().getValueAt(row, 3);
				// If new value null, reset it
				if (rating == null) {
					mTable.getModel().setValueAt(thumbedMovie.movie.rating, row, 3);
					return;
				} else {
					thumbedMovie.movie.rating = rating;
				}
				break;
			case 4:
				Integer votes = (Integer) mTable.getModel().getValueAt(row, 4);
				// If new value null, reset it
				if (votes == null) {
					mTable.getModel().setValueAt(thumbedMovie.movie.votes, row, 4);
					return;
				} else {
					thumbedMovie.movie.votes = votes;
				}
				break;
			default:
				System.err.println("Unrecognized column index to edit! " + e.getColumn());
				break;
		}

		// Update DB (synchronously)
		sDB.updateMovie(thumbedMovie.movie);

		// Update thumb if name was changed
		if (e.getColumn() == 1) {
			Runnable redraw = () -> ((DefaultTableModel) mTable.getModel()).fireTableCellUpdated(row, 0);
			ThumbFetcher.updateThumb(thumbedMovie.movie.name + " " + MOVIE_QUERY_POSTFIX,
					thumbedMovie.thumb, redraw);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(MoviesFrames::new);
	}

}
