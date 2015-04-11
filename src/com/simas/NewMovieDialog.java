package com.simas;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Created by Simas Abramovas on 2015 Apr 03.
 */

public class NewMovieDialog extends JDialog implements ActionListener, KeyListener {

	private static final String TITLE = "Add a new movie";
	private static final String NAME = "Name";
	private static final String YEAR = "Year";
	private static final String RATING = "Rating";
	private static final String VOTES = "Votes";
	private static final String OK = "OK";
	private static final String ADD = "->";
	private static final String REMOVE = "<-";
	private static final String MOVIE_ADDED = "Movie successfully added!";
	private static final String MOVIE_ADDITION_FAILED = "Failed to add a new movie!";
	private final JButton mOKButton;

	private JTextField mNameField, mYearField, mRatingField, mVotesField;
	private JList<DB.Actor> mAddedActors;

	public NewMovieDialog(MoviesFrames parent) {
		super(parent, TITLE);
		/* Component arrangement */
		JPanel mainPane = new JPanel();
		// Add padding to window
		mainPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));
		mainPane.setLayout(new GridBagLayout());
		setContentPane(mainPane);

		// Constraints
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 15, 15, 0);
		gbc.fill = GridBagConstraints.NONE;

		// Name
		add(new JLabel(NAME), gbc);
		++gbc.gridx;
		mNameField = new JTextField(30);
		mNameField.getDocument().addDocumentListener(new BorderResetter(mNameField));
		mNameField.addKeyListener(this);
		add(mNameField, gbc);

		// Year
		++gbc.gridy;
		gbc.gridx = 0;
		add(new JLabel(YEAR), gbc);
		++gbc.gridx;
		mYearField = new JTextField(30);
		mYearField.getDocument().addDocumentListener(new BorderResetter(mYearField));
		mYearField.addKeyListener(this);
		add(mYearField, gbc);

		// Rating
		++gbc.gridy;
		gbc.gridx = 0;
		add(new JLabel(RATING), gbc);
		++gbc.gridx;
		mRatingField = new JTextField(30);
		mRatingField.getDocument().addDocumentListener(new BorderResetter(mRatingField));
		mRatingField.addKeyListener(this);
		add(mRatingField, gbc);

		// Votes
		++gbc.gridy;
		gbc.gridx = 0;
		add(new JLabel(VOTES), gbc);
		++gbc.gridx;
		mVotesField = new JTextField(30);
		mVotesField.getDocument().addDocumentListener(new BorderResetter(mVotesField));
		mVotesField.addKeyListener(this);
		add(mVotesField, gbc);

		// Actor list
		++gbc.gridy;
		gbc.gridx = 1;
		// Parse all actors' "name surname"
		List<DB.Actor> actors = BaseFrame.sDB.selectActors(0, 0);
		// Create a list of all actors
		final JList<DB.Actor> actorList = new JList<>(actors.toArray(new DB.Actor[actors.size()]));
		actorList.setVisibleRowCount(4);
		JScrollPane scrollPane = new JScrollPane(actorList);
		add(scrollPane, gbc);

		// Added actor list
		gbc.gridx = 3;
		mAddedActors = new JList<>();
		mAddedActors.setVisibleRowCount(4);
		scrollPane = new JScrollPane(mAddedActors);
		add(scrollPane, gbc);

		// Add/Remove button
		gbc.gridx = 2;
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		// Add
		JButton button = new JButton(ADD);
		button.addActionListener(e -> {
			List<DB.Actor> actors1 = actorList.getSelectedValuesList();
			int addedActorCount = mAddedActors.getModel().getSize();
			for (int i = 0; i < addedActorCount; ++i) {
				actors1.add(mAddedActors.getModel().getElementAt(i));
			}
			mAddedActors.setListData(actors1.toArray(new DB.Actor[actors1.size()]));
		});
		buttonPanel.add(button);
		// Remove
		button = new JButton(REMOVE);
		button.addActionListener(e -> {
			// Parse added actors
			int addedActorCount = mAddedActors.getModel().getSize();
			List<DB.Actor> clearActors = new ArrayList<>();
			for (int i = 0; i < addedActorCount; ++i) {
				clearActors.add(mAddedActors.getModel().getElementAt(i));
			}
			// Remove selected actors (skip duplicates)
			List<DB.Actor> selectedActors = mAddedActors.getSelectedValuesList();
			for (DB.Actor selectedActor : selectedActors) {
				clearActors.remove(selectedActor);
			}
			// Update list
			mAddedActors.setListData(clearActors.toArray(new DB.Actor[clearActors.size()]));
		});
		buttonPanel.add(button);
		add(buttonPanel, gbc);

		// OK button
		++gbc.gridy;
		gbc.gridx = 0;
		mOKButton = new JButton(OK);
		mOKButton.addActionListener(this);
		add(mOKButton, gbc);

		pack();
		setLocationRelativeTo(null);
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		// Validate fields
		boolean fieldsAreValid = true;

		// Name
		String name = mNameField.getText();
		if (name.length() == 0) {
			mNameField.setBorder(BorderFactory.createLineBorder(Color.RED));
			fieldsAreValid = false;
		}

		// Year
		int year = 0;
		if (mYearField.getText().length() == 0) {
			mYearField.setBorder(BorderFactory.createLineBorder(Color.RED));
			fieldsAreValid = false;
		} else {
			try {
				year = Integer.parseInt(mYearField.getText());
			} catch (NumberFormatException e) {
				fieldsAreValid = false;
				mYearField.setBorder(BorderFactory.createLineBorder(Color.RED));
			}
		}

		// Rating
		double rating = 0.0;
		if (mRatingField.getText().length() == 0) {
			mRatingField.setBorder(BorderFactory.createLineBorder(Color.RED));
			fieldsAreValid = false;
		} else {
			try {
				rating = Double.parseDouble(mRatingField.getText());
			} catch (NumberFormatException e) {
				fieldsAreValid = false;
				mRatingField.setBorder(BorderFactory.createLineBorder(Color.RED));
			}
		}

		// Votes
		int votes = 0;
		if (mVotesField.getText().length() == 0) {
			mVotesField.setBorder(BorderFactory.createLineBorder(Color.RED));
			fieldsAreValid = false;
		} else {
			try {
				votes = Integer.parseInt(mVotesField.getText());
			} catch (NumberFormatException e) {
				fieldsAreValid = false;
				mVotesField.setBorder(BorderFactory.createLineBorder(Color.RED));
			}
		}

		if (fieldsAreValid) {
			// Add video to DB
			MoviesFrames moviesFrames = (MoviesFrames) getParent();
			DB.Movie movie = new DB.Movie(name, year, rating, votes);

			// Parse actors into a list
			int actorCount = mAddedActors.getModel().getSize();
			List<DB.Actor> actors = new ArrayList<>();
			for (int i=0; i<actorCount; ++i) {
				actors.add(mAddedActors.getModel().getElementAt(i));
			}

			// Execute transaction
			if (MoviesFrames.sDB.insertMovieWithActors(movie, actors)) {
				// Display success message
				JOptionPane.showMessageDialog(getParent(), MOVIE_ADDED, "",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				// Display fail message
				JOptionPane.showMessageDialog(getParent(), MOVIE_ADDITION_FAILED, "",
						JOptionPane.ERROR_MESSAGE);
			}
			setVisible(false);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {
		// If enter was pressed while on any of the fields, press OK
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			mOKButton.doClick();
		}
	}

	private final class BorderResetter implements DocumentListener {

		private final JComponent mComponent;
		private final Border mDefaultBorder;

		public BorderResetter(JComponent component) {
			mComponent = component;
			mDefaultBorder = mComponent.getBorder();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			mComponent.setBorder(mDefaultBorder);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			mComponent.setBorder(mDefaultBorder);
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			mComponent.setBorder(mDefaultBorder);
		}

	}

}
