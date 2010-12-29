import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import org.jnbt.*;


@SuppressWarnings("serial")
public class NBTBlockEditor extends JFrame{

	public static final int TILE_SIZE = 16;

	//keys
	public static final char UP = 'q';
	public static final char DOWN = 'z';
	public static final char BLOCK = 'b';

	private File currentFile = null;

	//schematic data
	private Point3D size;
	private Block[] blockData;

	private boolean objectLoaded = false;
	private int curLevel = 0;
	private String selectedBrush = "0";
	private int editMode = 0; //0 = view, 1 = edit

	//GUI objects
	private static NBTBlockEditor editor;
	private EditPanel editPanel = new EditPanel();
	private JLabel brushLabel;
	private JLabel selectedLabel;
	private JTextArea logTextArea = new JTextArea("Debug Log");
	private JScrollPane editorScrollPane;
	private JTabbedPane tabs;


	public NBTBlockEditor(){
		super ("NBTBlockEditor || pR0Ps");
		super.setPreferredSize(new Dimension (300, 300));
		super.setDefaultCloseOperation(EXIT_ON_CLOSE);
		super.setLayout (new BorderLayout());
		//super.setResizable(false);
		addToLog ("Program started\nLoading block images...");
		BlockData.initImages();
		addToLog ("Done");

		//attempt to adapt to the operating system's look
		try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
		catch (Exception e) {} //shit son, you get the crappy Java UI ;)

		//TODO: set up info boxes

		logTextArea.setEditable(false);
		logTextArea.setFocusable(false);

		//set up UI
		tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		JPanel mainTab = new JPanel(new BorderLayout());

		tabs.addKeyListener(new KeyPress());

		editorScrollPane = new JScrollPane(editPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		mainTab.add(editorScrollPane, BorderLayout.CENTER);
		//TODO: add UI to main panel

		tabs.add ("Main", mainTab);
		tabs.add ("Debug Log", new JScrollPane(logTextArea));
		tabs.setSelectedIndex(0);
		add(tabs);
		setJMenuBar(makeJMenuBar());
		pack();
		setVisible(true);
	}

	//sets up the menu bar
	public JMenuBar makeJMenuBar(){
		//set up the top menu
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenuItem openItem = new JMenuItem("Open");
		openItem.addActionListener(new ActionListener (){
			public void actionPerformed(ActionEvent arg0) {
				openFile();
			}
		});
		fileMenu.add(openItem);

		JMenuItem saveItem = new JMenuItem("Save");
		saveItem.addActionListener(new ActionListener (){
			public void actionPerformed(ActionEvent arg0) {
				saveFile();
			}
		});
		fileMenu.add(saveItem);

		JMenuItem saveAsItem = new JMenuItem("Save As");
		saveAsItem.addActionListener(new ActionListener (){
			public void actionPerformed(ActionEvent arg0) {
				saveFileAs();
			}
		});
		fileMenu.add(saveAsItem);

		JMenuItem closeItem = new JMenuItem("Close");
		closeItem.addActionListener(new ActionListener (){
			public void actionPerformed(ActionEvent arg0) {
				closeFile();
			}
		});
		fileMenu.add(closeItem);

		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener (){
			public void actionPerformed(ActionEvent arg0) {
				if (objectLoaded){
					closeFile();
				}
				System.exit(0);
			}
		});
		fileMenu.add(exitItem);
		menuBar.add(fileMenu);

		JMenu editMenu = new JMenu("Edit");
		JMenuItem blockItem = new JMenuItem("Choose Block");
		blockItem.addActionListener(new ActionListener (){
			public void actionPerformed(ActionEvent arg0) {
				chooseBrush();
			}
		});
		editMenu.add(blockItem);
		menuBar.add(editMenu);


		JMenu helpMenu = new JMenu ("Help");
		JMenuItem instructionsItem = new JMenuItem("Instructions");
		instructionsItem.addActionListener(new ActionListener (){
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(editor, "" +
						"", "Instructions", JOptionPane.PLAIN_MESSAGE);
			}
		});
		helpMenu.add(instructionsItem);

		JMenuItem aboutItem = new JMenuItem("About");
		aboutItem.addActionListener(new ActionListener (){
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(editor, "These are credits.\nCreated by pR0Ps", "About", JOptionPane.PLAIN_MESSAGE);
			}
		});
		helpMenu.add(aboutItem);
		menuBar.add(helpMenu);
		return menuBar;
	}

	//starts the program
	public static void main(String[] args) {
		editor = new NBTBlockEditor();
	}

	//GUI for closing the current file
	public void closeFile(){
		if (objectLoaded){
			int temp = JOptionPane.showConfirmDialog(editor, "Save before closing file?", "Save?", JOptionPane.YES_NO_CANCEL_OPTION);
			if (temp == JOptionPane.YES_OPTION){
				addToLog ("\nClosing without saving");
				close();
			}
			else if (temp == JOptionPane.NO_OPTION){
				addToLog ("\nClosing with saving");
				saveFileAs();
				close();
			}
			else if (temp == JOptionPane.CANCEL_OPTION){
				addToLog ("\nClose file cancelled");
			}
		}
	}

	//closes the current file
	private void close(){
		objectLoaded = false;
		editorScrollPane.repaint();
		addToLog("\nFile closed");
	}

	//GUI for opening a file
	public void openFile(){
		addToLog ("\nFile chooser opened");

		JFileChooser idChooser = new JFileChooser();
		idChooser.setDialogTitle("Choose the schematic file");

		if(idChooser.showOpenDialog(editor) == JFileChooser.APPROVE_OPTION){
			currentFile = idChooser.getSelectedFile();
			addToLog("\nFile '" + currentFile + "' picked (and is the current blockID file)");
			if (!loadData(currentFile)){
				currentFile = null;
				JOptionPane.showMessageDialog(editor, "There was an error reading the file", "No file loaded", JOptionPane.ERROR_MESSAGE);
			}
		}
		else{
			JOptionPane.showMessageDialog(editor, "No file loaded", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
			addToLog ("\nNo file loaded (cancelled by user)");
		}
		editPanel.newObjectLoaded();
	}

	//loads the data from the given file into the program
	private boolean loadData (File f){
		addToLog("\nGZiped File size is " + f.length() + " bytes\nLoading...");

		Map <String, Tag> schematicMap;
		byte[] data = {0};
		byte[] blocks = {0};

		try{
			NBTInputStream in = new NBTInputStream(new FileInputStream(f));
			Tag temp;
			//read and discard anything until the schematic tag
			try{
				while (true){
					temp = in.readTag();
					addToLog("aaaaa" + temp.toString());
					if (temp instanceof CompoundTag && temp.getName().equalsIgnoreCase("schematic")){
						schematicMap = (Map<String, Tag>) ((CompoundTag)temp).getValue();
						break;
					}
				}
			}
			catch (EOFException e){
				//End of file before hitting schematic tag
				addToLog("\nError: No 'Schematic' tag found");
				addToLog ("\nInvalid file");
				return false;
			}
			catch (Exception e){
				addToLog ("\nInvalid file");
				return false;
			}
			finally{
				in.close();
			}

			//assign values
			try{
				size = new Point3D();
				size.setZ((Short)schematicMap.get("Height").getValue());
				size.setX((Short)schematicMap.get("Width").getValue());
				size.setY((Short)schematicMap.get("Length").getValue());
				data = (byte[])schematicMap.get("Data").getValue();
				blocks = (byte[])schematicMap.get("Blocks").getValue();
			}
			catch (Exception e){
				addToLog("\nInvalid file: Needed components missing/improperly named");
				return false;
			}


			if (size.getX() * size.getY() * size.getZ() != data.length){
				addToLog("\nInvalid file: Dimensions do not match block data");
				return false;
			}
			else if (data.length != blocks.length){
				addToLog("\nInvalid file: blocks and block data size do not match (" + data.length +" vs " + blocks.length + " bytes)");
				return false;
			}
			addToLog("Data from file loaded, organizing...");
			blockData = new Block[data.length];
			for (int i = 0 ; i < data.length ; i++){
				blockData[i] = new Block(BlockData.toHex(blocks[i]), BlockData.toHex(data[i]));
			}
			objectLoaded = true;
			addToLog ("File loaded!");
			return true;
		}
		catch (Exception e){
			e.printStackTrace();
			addToLog("\nError: " + e.getMessage());
			addToLog ("\nFile not loaded");
			return false;
		}
	}

	//writes the data to the file
	public void writeData(File f){
		//TODO: save file
	}

	//saves the current file
	public void saveFile(){
		if (objectLoaded && currentFile != null){
			addToLog("\nSaving file...");
			writeData (currentFile);
		}
		else{
			addToLog("\nNo file loaded, cannot save");
		}
	}

	//GUI for choosing a file to save as
	public void saveFileAs(){
		//currentFile set to picked file
		//TODO: Save file as
	}

	//GUI for picking the block type to place
	public String chooseBrush(){
		//TODO: Choose block menu (use JDialog)
		return "2";
	}

	//translates a coordinate to its array index
	private int getIndex (int x, int y, int z){
		if (size != null){
			return z * size.getX() * size.getY() + y * size.getX() + x;
		}
		return -1;
	}

	//adds text to the debug log
	private void addToLog (String s){
		System.out.println (s);
		//logTextArea.append(s);
		//logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
	}


	//handles the keypresses
	private class KeyPress implements KeyListener{
		public void keyPressed(KeyEvent e) {}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {
			if (objectLoaded && tabs.getSelectedIndex() == 0){
				if (e.getKeyChar() == UP && curLevel < size.getZ() - 1){
					curLevel += 1;
					addToLog("\nMoved up to level " + curLevel);
					editorScrollPane.repaint();
				}
				else if (e.getKeyChar() == DOWN && curLevel > 0){
					curLevel -=1;
					addToLog("\nMoved down to level " + curLevel);
					editorScrollPane.repaint();
				}
				else if  (e.getKeyChar() == BLOCK){
					addToLog("\nChoosing a new block");
					chooseBrush();
				}
			}
		}
	}

	//handles all the graphics
	private class EditPanel extends JPanel{
		private BufferedImage background = null;
		private int x_size;
		private int y_size;

		public EditPanel(){
			//listen to mouse movements
			addMouseListener(new MouseListener(){
				public void mouseClicked(MouseEvent arg0) {
					// TODO Auto-generated method stub
				}
				public void mouseEntered(MouseEvent arg0) {
					// TODO Auto-generated method stub

				}
				public void mouseExited(MouseEvent arg0) {
					// TODO Auto-generated method stub

				}
				public void mousePressed(MouseEvent arg0) {
					// TODO Auto-generated method stub

				}
				public void mouseReleased(MouseEvent arg0) {
					// TODO Auto-generated method stub

				}
			});

		}

		//called when a new object is loaded. Resizes and repaints the interface
		private void newObjectLoaded(){
			if (objectLoaded){
				addToLog("\nNew object loaded");
				x_size = (TILE_SIZE + 1) * size.getX() + 1;
				y_size = (TILE_SIZE + 1) * size.getY() + 1;
				setPreferredSize(new Dimension (x_size, y_size));
				//set up the grid image
				this.background = new BufferedImage(x_size, y_size, BufferedImage.TYPE_INT_RGB);
				drawBG(background.getGraphics());
				repaint();
			}
		}

		//draws the background
		private void drawBG (Graphics g){
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, x_size, y_size);
		}

		private void drawBlocks (Graphics g){
			for (int x = 0 ; x < size.getX() ; x++){
				for (int y = 0 ; y < size.getY() ; y++){
					g.drawImage(blockData [getIndex(x, y, curLevel)].getTile(), x * (TILE_SIZE + 1) + 1, y * (TILE_SIZE + 1) + 1, null);
				}
			}
		}

		public void paintComponent(Graphics g){
			//editorScrollPane.validate();
			if (objectLoaded){
				g.drawImage(background, 0, 0, null);
				drawBlocks(g);
			}
		}
	}

}
