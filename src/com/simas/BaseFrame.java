package com.simas;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Created by Simas Abramovas on 2015 Apr 01.
 */

// Google ajax link -- &q=HTTPEncode.encode(keyword, "UTF-8")
    // https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=the%20godfather

// Maybe:
    // Actions with actors, acting and genres
    // Movies | Actors --- tabs. Abstract MoviesFrames - to show either movies or actors of some movie
    // Fetch images on scroll
    // Show movies specific actor (mActor in MoviesFrame).

public abstract class BaseFrame extends JFrame {

    public static final String APP_NAME = "Top Movies";
    private static int sOffset;
    private static final int OFFSET_STEP = 30;
    private static final int MAX_OFFSET = 210;
    /**
     * Single DB instance for all {@code JFrame}s
     */
    protected static DB sDB;

    public BaseFrame() {
        super(APP_NAME);
        if (sDB == null) sDB = new DB();
        addComponents();
        customizeFrame();
    }

    public abstract void addComponents();

    /**
     * Basic {@code Frame} customization
     */
    private void customizeFrame() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setUndecorated(false);
        resizeToFitInScreen(this, (double) 2 / 5);

        // Move the frame so others are visible too
        setLocation(getX() + sOffset, getY() + sOffset);
        sOffset += OFFSET_STEP;
        if (sOffset > MAX_OFFSET) {
            sOffset = 0;
        }
    }

    /**
     * Resize the{@code }JFrame} to fit the screen at specified proportions.
     * @param frame     Window that will be resized
     * @param atMost    Part of the window that can be covered at most.
     */
    static void resizeToFitInScreen(Window frame, double atMost) {
        // Check image sizes
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double maxWidth = screenSize.getWidth() * atMost;
        double maxHeight = screenSize.getHeight() * atMost;
        frame.setSize((int) maxWidth, (int) maxHeight);
        frame.setLocationRelativeTo(null); // Center frame on the screen
    }

}
