package edu.eci.arsw.dogsrace.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/**
 * Interfaz de usuario y modelo para un Canodromo
 * 
 * @author rlopez
 * 
 */
public class Canodromo extends JFrame {
	private static final long serialVersionUID = 1L;

	/**
	 * Carriles del canodromo
	 */
	private Carril[] carril;

	private JButton butStart = new JButton("Start");
	private JButton butStop = new JButton("Stop");
	private JButton butContinue = new JButton("Continue");
	private JButton	butRestart = new JButton("Restart");
	/**
	 * Constructor
	 * 
	 * @param nCarriles
	 *            Numero de carriles
	 * @param longPista
	 *            Longitud de la pista
	 */
	public Canodromo(int nCarriles, int longPista) {
		carril = new Carril[nCarriles];
		for (int i = 0; i < carril.length; i++) {
			carril[i] = new Carril(longPista, "" + i);
		}

		JPanel cont = (JPanel) getContentPane();
		cont.setLayout(new BorderLayout());

		JPanel panPistas = new JPanel();
		panPistas.setLayout(new GridLayout(nCarriles, 1));

		JPanel panCarril;
		BorderLayout bLay;
		GridLayout gridTrack = new GridLayout(1, longPista);
		for (int row = 0; row < nCarriles; row++) {
			bLay = new BorderLayout();
			panCarril = new JPanel();
			panCarril.setLayout(bLay);
			JPanel panTrack = new JPanel();
			panTrack.setLayout(gridTrack);

			for (int col = 0; col < longPista; col++) {
				panTrack.add(carril[row].getPaso(col));
			}
			panCarril.add(panTrack, BorderLayout.CENTER);
			panCarril.add(carril[row].getLlegada(), BorderLayout.EAST);
			panPistas.add(panCarril);
		}

		panPistas.setBorder(new EmptyBorder(new Insets(5, 0, 5, 0)));

		// Wrap pistas in a scrollpane for many lanes
		JScrollPane scrollPane = new JScrollPane(panPistas);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		cont.add(scrollPane, BorderLayout.CENTER);

		JPanel butPanel = new JPanel();
		butPanel.setLayout(new FlowLayout());
		butPanel.add(butStart);
		butPanel.add(butStop);
		butPanel.add(butContinue);
		butPanel.add(butRestart);
		cont.add(butPanel, BorderLayout.SOUTH);

		// Calculate ideal size but limit to 90% of screen
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int maxWidth = (int) (screenSize.width * 0.9);
		int maxHeight = (int) (screenSize.height * 0.9);
		
		int butWidht = 8;
		int butHeight = 20;
		int idealWidth = butWidht * longPista;
		int idealHeight = butHeight * nCarriles + 150;
		
		// Use smaller of ideal or max dimensions
		int finalWidth = Math.min(idealWidth, maxWidth);
		int finalHeight = Math.min(idealHeight, maxHeight);
		
		this.setSize(finalWidth, finalHeight);
		
		// Center on screen
		int x = (screenSize.width - finalWidth) / 2;
		int y = (screenSize.height - finalHeight) / 2;
		this.setLocation(x, y);
		this.setTitle("Canodromo");

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	/**
	 * Reinicia cada uno de los carriles
	 */
	public void restart() {
		for (int i = 0; i < carril.length; i++) {
			carril[i].reStart();
		}
	}

	/**
	 * Retorna un carril
	 * 
	 * @param i
	 *            Numero del carril
	 * @return
	 */
	public Carril getCarril(int i) {
		return carril[i];
	}

	public int getNumCarriles() {
		return carril.length;
	}

	/**
	 * Asocia una accion con el boton de start
	 * 
	 * @param action
	 */
	public void setStartAction(ActionListener action) {
		butStart.addActionListener(action);
	}

	/**
	 * Asocia una accion con el boton de stop
	 * 
	 * @param action
	 */
	public void setStopAction(ActionListener action) {
		butStop.addActionListener(action);
	}

	/**
	 * Asocia una accion con el boton de continuar
	 * 
	 * @param action
	 */
	public void setContinueAction(ActionListener action){
		butContinue.addActionListener(action);
	}

	public void SetRestartAction(ActionListener action){
		butRestart.addActionListener(action);

	}
	
	public void winnerDialog(String winner,int total) {
            JOptionPane.showMessageDialog(null, "El ganador fue:" + winner + " de un total de " + total);
        }

	public void enableStart(boolean enabled) {
		butStart.setEnabled(enabled);
	}

}
