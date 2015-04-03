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

public class ActorsFrame extends BaseFrame implements TableModelListener {

	private List<ThumbedActor> mThumbedActors = new ArrayList<>();
	private static final String[] COLUMN_NAMES = { "Poster", "Name", "Surname" };
	private static final String OPTIONS_TITLE = "Options";
	private static final String DELETE_TITLE = "Delete actor";
	private static final String ADD = "Add";
	private static final String SEARCH = "Search";
	private static final int INITIAL_ACTOR_COUNT = 1;
	private final DB.Movie mMovie;

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

	public ActorsFrame(DB.Movie movie) {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		mMovie = movie;
		new Thread(() -> {
			populateTable(null);
		}).start();
	}

	public ActorsFrame() {
		mMovie = null;
		new Thread(() -> {
			populateTable(null);
		}).start();
		setVisible(true);
	}

	private class ThumbedActor {
		DB.Actor actor;
		ImageIcon thumb;
	}

	@Override
	public void addComponents() {
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

		/* Top Panel (new movie + search) */
//		JButton addButton = new JButton(ADD);
//		addButton.addActionListener(e -> new NewMovieDialog(ActorsFrame.this).setVisible(true));
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
//		topPanel.add(addButton, BorderLayout.WEST);
		topPanel.add(searchPanel, BorderLayout.EAST);
		topPanel.setSize(500, 100);
		add(topPanel);
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
		add(scrollPane, BorderLayout.CENTER);
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
					Object[][] data = new Object[mThumbedActors.size()][];
					int i = 0;
					for (ThumbedActor tm : mThumbedActors) {
						data[i++] = new Object[]{tm.thumb, tm.actor.name, tm.actor.surname};
					}
					((DefaultTableModel) mTable.getModel()).setDataVector(data, COLUMN_NAMES);
				}
			}
		});

		/* Right click menu */
		mRightClickMenu = new JPopupMenu(OPTIONS_TITLE);
		// Menu items
		JMenuItem deleteRow = new JMenuItem(DELETE_TITLE);
		deleteRow.addActionListener(e -> {
			int row = mTable.getSelectedRow();

			if (sDB.deleteActor(mThumbedActors.get(row).actor)) {
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
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}
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
				Collections.sort(mThumbedActors,
						(o1, o2) -> o1.actor.name.compareToIgnoreCase(o2.actor.name) * mOrder);
				break;
			case 2:
				Collections.sort(mThumbedActors,
						(o1, o2) -> o1.actor.surname.compareToIgnoreCase(o2.actor.surname)*mOrder);
				break;
			default:
				System.err.println("Unrecognized column index to sort! " + index);
				break;
		}
	}

	public void populateTable(String query) {
		List<DB.Actor> actors;
		if (query == null || query.length() == 0) {
			if (mMovie == null) {
				actors = sDB.selectActors(INITIAL_ACTOR_COUNT, 0);
			} else {
				actors = sDB.selectActorsInMovie(mMovie, INITIAL_ACTOR_COUNT, 0);
			}
		} else {
			if (mMovie == null) {
			actors = sDB.findActors(query);
			} else {
				actors = sDB.findActorsInMovie(mMovie, query);
			}
		}

		mThumbedActors = new ArrayList<>();
		DefaultTableModel model = new DefaultTableModel();

		int i = 0;
		for (DB.Actor actor : actors) {
			ThumbedActor ta = new ThumbedActor();
			ta.actor = actor;
			final int row = i++;
			ta.thumb = ThumbFetcher.getImageIcon(actor.name + " " + actor.surname,
					() -> model.fireTableCellUpdated(row, 0));
			mThumbedActors.add(ta);
		}

		// Convert to 2d array
		Object[][] data = new Object[mThumbedActors.size()][];
		i=0;
		for (ThumbedActor ta : mThumbedActors) {
			data[i++] = new Object[] { ta.thumb, ta.actor.name, ta.actor.surname };
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
		ThumbedActor thumbedActor = mThumbedActors.get(row);

		switch (e.getColumn()) {
			case 1:
				String name = (String) mTable.getModel().getValueAt(row, 1);
				// If new value is empty, reset it
				if (name == null || name.length() == 0) {
					mTable.getModel().setValueAt(thumbedActor.actor.name, row, 1);
					return;
				} else {
					thumbedActor.actor.name = name;
				}
				break;
			case 2:
				String surname = (String) mTable.getModel().getValueAt(row, 2);
				// If new value is empty, reset it
				if (surname == null || surname.length() == 0) {
					mTable.getModel().setValueAt(thumbedActor.actor.name, row, 2);
					return;
				} else {
					thumbedActor.actor.surname = surname;
				}
				break;
			default:
				System.err.println("Unrecognized column index to edit! " + e.getColumn());
				break;
		}

		// Update DB (synchronously)
		sDB.updateActor(thumbedActor.actor);

		// Update thumb if name was changed
		if (e.getColumn() == 1) {
			Runnable redraw = () -> ((DefaultTableModel) mTable.getModel()).fireTableCellUpdated(row, 0);
			ThumbFetcher.updateThumb(thumbedActor.actor.name + " " + thumbedActor.actor.surname,
					thumbedActor.thumb, redraw);
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(ActorsFrame::new);
	}

}
