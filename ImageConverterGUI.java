import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ImageConverterGUI implements ActionListener {

	// client code
	public static void main(String[] args) {
		System.out.println("hello");
		ImageConverterGUI gui = new ImageConverterGUI();
	}
	
	// constants
	private static final int WIDTH = 550; // large enough to display all text
	private static final int HEIGHT = 650;
	private static final int MAXWIDTH = 1350; // roughly size of my screen
	private static final int MAXHEIGHT = 820;
	private static Color backgroundColor = new Color(0x00, 0x99, 0x99);
	private static Color firstColor = new Color(0x00, 0x99, 0x99);
	private static Color secondColor = new Color(0xFF, 0x74, 0x00);
	private Font resultFont = new Font("Serif", Font.PLAIN, 2);
	private Font labelFont = new Font("Verdana", Font.ROMAN_BASELINE, 15);
	private Font titleFont = new Font("Verdana", Font.BOLD, 28);
	
	// fields for GUI components
	private JFrame frame;
	private JPanel northPanel;
	private JPanel titlePanel;
	private JPanel signaturePanel;
	private JPanel southPanel;
	private JPanel centerPanel;
	
	private JLabel title;
	private JLabel signature;
	private JLabel optionLabel;
	private JLabel visibleImage;
	
	private JButton asciiButton;
	private JButton grayscaleButton;
	private JButton negativeButton;
	private JButton originalButton;
	private JButton uploadButton;
	private JButton restartButton;
	
	private ImageIcon placeholderIcon;
	private JTextArea resultsArea;
	private JFileChooser fileUpload;
	
	// for the ascii converter
	private BufferedImage image;
	private Image img;
    private PrintWriter printWriter;
    private FileWriter fileWriter;
    private File resultFile;
    private File file;
    private FileReader reader;
    private BufferedReader br;
    private int scale; // scales ascii output
    					// larger values -> smaller output
    
    // for the gray-scale converter
    private static BufferedImage imgGray;
    
    // for the negative converter
    private static BufferedImage imgNeg;
	
    // constructor
	public ImageConverterGUI() {
		
		// set up the main container
		frame = new JFrame("Image Converter");
		frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		frame.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		frame.setMaximumSize(new Dimension(MAXWIDTH, MAXHEIGHT));
		frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
		frame.setBackground(backgroundColor);
		frame.setLocation(new Point(50, 50));
		frame.setLayout(new BorderLayout()); // sets the layout
		
		// top title with our signature and reset button
		northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		titlePanel = new JPanel();
		title = new JLabel("Image Converter App");
		title.setFont(titleFont);
		signaturePanel = new JPanel();
		signaturePanel.setBackground(secondColor);
		signature = new JLabel("By Jack Mulrow and Luis Mejia");
		titlePanel.add(title);
		titlePanel.setBackground(firstColor);
		signaturePanel.add(signature);
		restartButton = new JButton("Reset");
		restartButton.addActionListener(this);
		restartButton.setFocusable(false); // style
		signaturePanel.add(restartButton);
		northPanel.add(titlePanel);
		northPanel.add(signaturePanel);
		frame.add(northPanel, BorderLayout.NORTH);
		
		// display in center for images and conversions
		centerPanel = new JPanel(new BorderLayout());
		placeholderIcon = new ImageIcon("placeholder.gif");
		visibleImage = new JLabel(placeholderIcon);
		centerPanel.add(visibleImage, BorderLayout.CENTER);
		resultsArea = new JTextArea();
		resultsArea.setFont(resultFont);
		frame.add(centerPanel, BorderLayout.CENTER);

		// bottom file upload then format selection buttons
		southPanel = new JPanel(new FlowLayout());
		southPanel.setBackground(firstColor);
        optionLabel = new JLabel("Choose an image to convert "
        		+ "(JPG, GIF, PNG):");
        optionLabel.setFont(labelFont);
	    southPanel.add(optionLabel);
		uploadButton = new JButton("Select a File");
		uploadButton.addActionListener(this);
		southPanel.add(uploadButton);
		fileUpload = new JFileChooser();
		asciiButton = new JButton("ASCII");
		asciiButton.addActionListener(this);
		grayscaleButton = new JButton("Grayscale");
		grayscaleButton.addActionListener(this);
		negativeButton = new JButton("Negative");
		negativeButton.addActionListener(this);
		originalButton = new JButton("Original");
		originalButton.addActionListener(this);
		frame.add(southPanel, BorderLayout.SOUTH);
		
		// final formatting and setting default button
		frame.getRootPane().setDefaultButton(uploadButton);
		frame.pack();
		frame.setVisible(true);
		
	} // end of constructor
	
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == uploadButton) {
	        int returnVal = fileUpload.showOpenDialog(frame);
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            file = fileUpload.getSelectedFile();
	            try {
	            	img = ImageIO.read(file);
	            	image = (BufferedImage) img;
	            } catch (IOException e) {
	            	error();
	            }
	            visibleImage.setIcon(new ImageIcon(img));
	            if (image.getWidth() > WIDTH-50  // adjust frame size for 
	            		|| image.getHeight() > HEIGHT-150) { // big images
	            		frame.setSize(new Dimension(image.getWidth()+100,
	            				image.getHeight()+200));
	            }
	    		// remove file upload buttons and add format buttons
	    		optionLabel.setText("Select conversion:");
	    		southPanel.add(asciiButton);
	    		southPanel.add(grayscaleButton);
	    		southPanel.add(negativeButton);
	    		southPanel.add(originalButton);
	    		southPanel.remove(uploadButton);
	    		southPanel.repaint();
	        }
		}
		else if (event.getSource() == asciiButton) {
			// ask for ascii output scale
			int response = JOptionPane.showOptionDialog(frame, "Choose "
					+ "size of Ascii output", "Ascii Output Scale", 
					JOptionPane.INFORMATION_MESSAGE, 
					JOptionPane.YES_NO_CANCEL_OPTION, null, new String[]
							{"Largest", "Medium", "Smallest"}, "Medium");
			if (response == JOptionPane.YES_OPTION) {scale = 1;}
			else if (response == JOptionPane.NO_OPTION) {scale = 2;}
			else {scale = 5;}
			// run the conversion program
			try {
				convertToAscii();
		    	reader = new FileReader(resultFile);
		        br = new BufferedReader(reader);
			} catch (IOException e) {
				error();
			}
			centerPanel.removeAll();
			centerPanel.add(resultsArea, BorderLayout.CENTER);
			try {
				resultsArea.read(br, null);
			} catch (IOException e) {
				error();
			}
			centerPanel.repaint();	
		}
		else if (event.getSource() == grayscaleButton) {
			makeGray();
			// display gray-scale image
			centerPanel.removeAll();
			centerPanel.add(visibleImage, BorderLayout.CENTER);
			visibleImage.setIcon(new ImageIcon(imgGray));
		}
		else if (event.getSource() == negativeButton) {
			makeNegative();
			// display negative color image
			centerPanel.removeAll();
			centerPanel.add(visibleImage, BorderLayout.CENTER);
			visibleImage.setIcon(new ImageIcon(imgNeg));
		}
		else if (event.getSource() == originalButton) {
			// display the original image
			centerPanel.removeAll();
			centerPanel.add(visibleImage, BorderLayout.CENTER);
			visibleImage.setIcon(new ImageIcon(img));
		}
		else if (event.getSource() == restartButton) {
			reset();
		}
	} // end of method

	// converts picture to ascii text file
    private void convertToAscii() throws IOException {
    	resultFile = new File("results.txt");
        printWriter = new PrintWriter(fileWriter
        		= new FileWriter(resultFile));
        for (int i = 0; i < image.getHeight(); i += scale) { // pixel
            for (int j = 0; j < image.getWidth(); j += scale) { // sampling
                Color pixelColor = new Color(image.getRGB(j, i));
                double pixelVal = (((pixelColor.getRed() // color density
                		* 0.30) + (pixelColor.getBlue() * 0.59) 
                		+ (pixelColor.getGreen() * 0.11)));
                printCharacter(asciiCharacter(pixelVal));
            }
            printWriter.println("");
            printWriter.flush();
            fileWriter.flush();
        }
    } // end of method

    // returns certain characters for different pixel densities
    private String asciiCharacter(double pixel) {
        String s = " ";
        if (pixel >= 240) {
            s = " ";
        } 
        else if (pixel >= 210) {
            s = ".";
        } 
        else if (pixel >= 190) {
            s = "^";
        } 
        else if (pixel >= 170) {
            s = "+";
        } 
        else if (pixel >= 120) {
            s = "*";
        } 
        else if (pixel >= 110) {
            s = "&";
        } 
        else if (pixel >= 80) {
            s = "8";
        }
        else if (pixel >= 60) {
            s = "#";
        } 
        else {
            s = "@";
        }
        return s;
    } // end of method
    
    // prints the ascii character to a text file
    private void printCharacter(String s) throws IOException {
    	printWriter.print(s);
    	printWriter.flush();
    	fileWriter.flush();
    } // end of method

    // creates gray-scale version of image
    private void makeGray(){
    	imgGray = new BufferedImage(image.getWidth(),
    			image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    	for(int x = 0; x < image.getWidth(); x++){
    		for(int y = 0; y < image.getHeight(); y++){
    			int rgb = image.getRGB(x,y);
    			int r = (rgb >> 16) & 0xFF;
    			int g = (rgb >> 8) & 0xFF;
    			int b = (rgb & 0xFF);
          
    			int gray = (r + b + g)/3;
    			int newGray = (gray << 16) + (gray << 8) + gray;
    			imgGray.setRGB(x, y, newGray);
    		}
    	}
    	try {
    		File grayFile = new File("grayImage.jpg");
    		ImageIO.write(imgGray, "jpg", grayFile);
    	} catch (IOException e) {
    		error();
    	}
    } // end of method

    // creates color negative version of image
    private void makeNegative(){
    	imgNeg = new BufferedImage(image.getWidth(),
    			image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    	for (int x = 0; x < image.getWidth(); x++){
    		for (int y = 0; y < image.getHeight(); y++){
    			int rgb = image.getRGB(x,y);
    			int r = (rgb >> 16) & 0xFF;
    			int g = (rgb >> 8) & 0xFF;
    			int b = (rgb & 0xFF);
          
    			int newR = 255 - r;
    			int newG = 255 - g;
    			int newB = 255 - b;
    			int newCol = (newR << 16) | (newG << 8) | newB;
    			imgNeg.setRGB(x, y, newCol);
    		}
    	}
    	try {
    		File negFile = new File("negImage.jpg");
    		ImageIO.write(imgNeg, "jpg", negFile);
    	} catch (IOException e) {
    		error();
    	}
    } // end of method
    
    // resets the GUI
    private void reset() {
    	// reset the bottom panel
    	southPanel.removeAll();
        optionLabel.setText("Choose an image to convert (JPG, GIF, PNG):");
	    southPanel.add(optionLabel);
		southPanel.add(uploadButton);
		southPanel.repaint();
		
		// reset the display
		centerPanel.removeAll();
		visibleImage.setIcon(placeholderIcon);
		centerPanel.add(visibleImage, BorderLayout.CENTER);
		centerPanel.repaint();
		
		// resize the display
		frame.setSize(new Dimension(WIDTH, HEIGHT));
		
		// highlight upload button
		frame.getRootPane().setDefaultButton(uploadButton);
    } // end of method
    
    // An optional error program called when an exception is caught
    // shouldn't ever be seen though
    private void error() {
    	// do nothing
    } // end of method
} // end of class