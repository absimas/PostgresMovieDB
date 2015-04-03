package com.simas;

import com.sun.glass.ui.Size;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Created by Simas Abramovas on 2015 Apr 02.
 */

public class ThumbFetcher {

	private static final String GOOGLE_IMAGE_API_FORMAT =
			"https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=%s";
	private static final String LOADING_IMAGE_FILENAME = "loading.png";
	private static final String FAIL_IMAGE_FILENAME = "fail.png";
	private static Image LOADING_IMAGE;
	private static Image FAIL_IMAGE;
	public static final Size MAX_THUMB_SIZE = new Size(150, 150);

	/**
	 * Used to avoid google restrictions (100 queries / day). If empty won't use a proxy.
	 */
	private static final HashMap<String, Integer> PROXIES = new HashMap<String, Integer>() {{
//		put("158.85.49.233", 80);
//		put("108.166.205.163", 3128);
//		put("206.214.126.233", 8080);
//		put("54.174.90.28", 3128);
//		put("54.64.75.254", 80);
//		put("54.64.60.6", 80);
//		put("98.191.74.6", 80);
//		put("209.66.192.149", 80);
//		put("98.191.74.30", 80);
//		put("69.57.48.251", 8080);
	}};
	/**
	 * Retry 3 times or loop through proxies.
	 */
	private static final int MAX_RETRY_COUNT = (PROXIES.size() == 0) ? 1 : PROXIES.size();

	static {
		try {
			LOADING_IMAGE = ImageIO.read(new File(LOADING_IMAGE_FILENAME));
			if (LOADING_IMAGE.getWidth(null) != -1 &&  LOADING_IMAGE.getHeight(null) != -1) {
				Image scaledImage = AsyncImageLoader.scaleImage(LOADING_IMAGE);
				if (scaledImage != null) {
					LOADING_IMAGE = scaledImage;
				}
			}

			FAIL_IMAGE = ImageIO.read(new File(FAIL_IMAGE_FILENAME));
			if (FAIL_IMAGE.getWidth(null) != -1 &&  FAIL_IMAGE.getHeight(null) != -1) {
				Image scaledImage = AsyncImageLoader.scaleImage(FAIL_IMAGE);
				if (scaledImage != null) {
					FAIL_IMAGE = scaledImage;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ThumbFetcher() {
		// Un-constructable
	}

	public static ImageIcon getImageIcon(String keyword, Runnable updateListener) {
		final ImageIcon thumb = new ImageIcon(LOADING_IMAGE);

		// Asynchronously fetch the thumb
		new AsyncImageLoader(keyword, thumb, updateListener, 0).execute();

		// Return an empty thumb (will change its image after runnable done)
		return thumb;
	}

	public static void updateThumb(String keyword, ImageIcon thumb, Runnable updateListener) {
		thumb.setImage(LOADING_IMAGE);
		updateListener.run();

		// Asynchronously fetch the thumb
		new AsyncImageLoader(keyword, thumb, updateListener, 0).execute();
	}

	private static class AsyncImageLoader extends SwingWorker<Image, Object>
			implements ImageObserver {

		private final String mKeyword;
		private final ImageIcon mIcon;
		private final Runnable mUpdateListener;
		private boolean mEdited;
		/**
		 * Retry number. Corresponds to the google's ajax result that will be fetched
		 */
		private int mRetry;

		public AsyncImageLoader(String keyword, ImageIcon targetIcon, Runnable updateListener,
		                        int retry) {
			mKeyword = keyword;
			mIcon = targetIcon;
			mUpdateListener = updateListener;
			mRetry = retry;
		}

		@Override
		protected Image doInBackground() throws Exception {
			Image image;
			try {
				// Image URL
				String imageUrl = getImageUrlFromGoogle(mKeyword, mRetry);
				if (imageUrl == null) return null;
				URL url = new URL(imageUrl);

				// If max retry count reached negate it so it won't loop further
				if (mRetry == MAX_RETRY_COUNT) mRetry = -1;

				// Fetch image from url
				Image unscaledImage = ImageIO.read(url);
				if (unscaledImage == null) {
					return null;
				}

				// Check the width and height
				if (unscaledImage.getWidth(this) == -1 || unscaledImage.getHeight(this) == -1) {
					return LOADING_IMAGE;
				}

				image = scaleImage(unscaledImage);
			} catch (IOException e) {
				System.err.println(e.getMessage());
				image = null;
			}

			if (mRetry >= 0 || image != null) {
				return image;
			} else {
				return FAIL_IMAGE;
			}
		}

		@Override
		protected void done() {
			super.done();
			try {
 				Image image = get();
				if (image != null) {
					setImage(image);
					return;
				}
			} catch (InterruptedException | ExecutionException e) {
				System.err.println(e.getMessage());
			}
			if (mRetry >= 0) {
				System.out.println("Retrying image fetch...");
				new AsyncImageLoader(mKeyword, mIcon, mUpdateListener, ++mRetry).execute();
			} else {
				System.err.println("Image couldn't be fetched!");
			}
		}

		private String getImageUrlFromGoogle(String keyword, int resultIndex) {
			String ajaxUrl;
			try {
				ajaxUrl = String.format(GOOGLE_IMAGE_API_FORMAT, URLEncoder.encode(keyword, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}
			String imageUrl = null;
			// Loop until a connection is made or we're all out of proxies
			URLConnection connection = createConnection(ajaxUrl);

			if (connection == null) return null;
			try {
				InputStream in = connection.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				StringBuilder builder = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				String response = builder.toString();
				// Parse JSON
				JSONObject json = new JSONObject(response);

				JSONObject responseData = json.getJSONObject("responseData");
				JSONArray results = responseData.getJSONArray("results");
				JSONObject result = results.getJSONObject(resultIndex);
				imageUrl = result.getString("url");
			} catch (IOException | JSONException e) {
				System.err.println(e.getMessage());
			}

			return imageUrl;
		}

		private URLConnection createConnection(String ajaxUrl) {
			URLConnection connection = null;

			// Loop proxies if have any
			if (PROXIES.size() > 0) {
				Iterator proxyIterator = PROXIES.entrySet().iterator();
				// Skip failed proxies
				int failCount = mRetry;
				while (failCount > 0 && proxyIterator.hasNext()) {
					--failCount;
					proxyIterator.next();
				}
				while (connection == null && proxyIterator.hasNext()) {
					try {
						// Init URL
						URL url = new URL(ajaxUrl);

						// Init proxy
						Map.Entry pair = (Map.Entry) proxyIterator.next();
						Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
								(String) pair.getKey(), (Integer) pair.getValue()));

						// Open url via proxy
						connection = url.openConnection(proxy);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				try {
					// Init URL
					URL url = new URL(ajaxUrl);

					// Open url via proxy
					connection = url.openConnection();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return connection;
		}

		@Override
		public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
			if (!mEdited) {
				Image scaledImage = scaleImage(img);
				if (scaledImage != null) {
					setImage(scaledImage);
				} else {
					if (mRetry >= 0) {
						System.out.println("Retrying image fetch...");
						new AsyncImageLoader(mKeyword, mIcon, mUpdateListener, ++mRetry).execute();
						return true;
					} else {
						setImage(FAIL_IMAGE);
					}
				}
				mEdited = true;
			}
			return false;
		}

		private void setImage(Image image) {
			SwingUtilities.invokeLater(() -> {
				if (mIcon.getImage() != image) {
					mIcon.setImage(image);
					mUpdateListener.run();
				}
			});
		}

		private static Image scaleImage(Image img) {
			// Check if size for thumb is calculated
			Size thumbSize = getThumbSize(img.getWidth(null), img.getHeight(null));
			if (thumbSize == null) {
				System.err.println("Thumb measurements failed!");
				return null;
			}

			// Scale and return the image
			Image scaled = img.getScaledInstance(thumbSize.width, thumbSize.height, Image.SCALE_SMOOTH);
			// Force image measuring
			int height = scaled.getHeight(null);
			int width = scaled.getWidth(null);
			if (height > MAX_THUMB_SIZE.height || height < 1 || width > MAX_THUMB_SIZE.width ||
					width < 1 ) {
				scaled = null;
			}

			return scaled;
		}

		private static Size getThumbSize(int width, int height) {
			if (width <= 0 || height <= 0) {
				return null;
			}

			int newWidth, newHeight;
			double modifier;

			if (width > height && width > MAX_THUMB_SIZE.width) {
				// Too big, will scale to fit width
				modifier = MAX_THUMB_SIZE.width / (double) width;
			} else if (height > width && height > MAX_THUMB_SIZE.height) {
				// Too big, will scale to fit height
				modifier = MAX_THUMB_SIZE.height / (double) height;
			} else {
				// Too small
				if (width > height) {
					// Will scale to fit width
					modifier = (double) width / MAX_THUMB_SIZE.width;
				} else {
					// Will scale to fit height
					modifier = (double) height / MAX_THUMB_SIZE.height;
				}
			}
			newWidth = (int) Math.floor(width * modifier);
			newHeight = (int) Math.floor(height * modifier);

			return new Size(newWidth, newHeight);
		}

	}

}
