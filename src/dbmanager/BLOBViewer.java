package dbmanager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class BLOBViewer extends JDialog {
	private JLabel iconLabel;

	public BLOBViewer() {
		jbInit();
	}

	private void jbInit() {
		setTitle("BLOB Viewer");
		setPreferredSize(new Dimension(200, 200));
		setMaximumSize(new Dimension(2000, 2000));

		iconLabel = new JLabel("BLOB Imagen");
		getContentPane().add(iconLabel, BorderLayout.CENTER);
		iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
	}

	void setImage(ImageIcon icon) {
		iconLabel.setIcon(icon);
	}

}
