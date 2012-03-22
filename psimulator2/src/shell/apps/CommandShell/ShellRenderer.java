/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package shell.apps.CommandShell;

import exceptions.TelnetConnectionException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import logging.Logger;

import logging.LoggingCategory;
import shell.ShellUtils;
import telnetd.io.BasicTerminalIO;
import telnetd.io.TerminalIO;
import telnetd.io.toolkit.ActiveComponent;

/**
 *
 * @author Martin Lukáš
 */
public class ShellRenderer extends ActiveComponent {

	private CommandShell commandShell;
	// private BasicTerminalIO m_IO;   // no need for this. Parent class Component has same protected member
	private int cursor = 0;
	private StringBuilder sb = new StringBuilder(50); //buffer načítaného řádku, čtecí buffer
	private History history = new History();
	/**
	 * flag signaling if line is returned... if ctrl+c is read then no line is returned
	 */
	private boolean returnValue = true;

	public ShellRenderer(CommandShell commandShell, BasicTerminalIO termIO, String name) {
		super(termIO, name);
		this.commandShell = commandShell;
		// this.termIO = termIO;  // no need for this. Parent class Component has same protected member
	}

	public History getHistory() {
		return history;
	}

	public void setHistory(History history) {
		this.history = history;
	}

	/**
	 * hlavní funkce zobrazování shellu a čtení z terminálu, reakce na různé klávesy ENETER, BACKSCAPE, LEFT ....
	 *
	 * @return vrací přečtenou hodnotu z řádku, příkaz
	 * @throws TelnetConnectionException
	 */
	public void run() throws Exception {

		this.sb.setLength(0); // clear string builder
		returnValue = true;
		boolean stop = false; // příznak pro ukončení čtecí smyčky jednoho příkazu
		List<String> nalezenePrikazy = new LinkedList<String>(); // seznam nalezenych příkazů po zmáčknutí tabu
		this.cursor = 0;



		while (!stop) {

			try {

				int inputValue = 0;

				try {
					inputValue = this.m_IO.read();
				} catch (SocketTimeoutException ex) {
					inputValue = 0;
				}

				if (inputValue == 0) {
					continue;
				}

				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečetl jsem jeden znak: " + inputValue);

				if (ShellUtils.isPrintable(inputValue)) {  // is a regular character like abc...
					Logger.log(Logger.DEBUG, LoggingCategory.TELNET, " Tisknul jsem znak: " + String.valueOf((char) inputValue) + " ,který má kód: " + inputValue);
					m_IO.write(inputValue);
					sb.insert(cursor, (char) inputValue);
					cursor++;
					draw();
					Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Pozice kurzoru: " + cursor);
					continue; // continue while
				}

				if (inputValue != TerminalIO.TABULATOR) {
					nalezenePrikazy = new LinkedList<String>(); // vyčistím pro další hledání
				}

				if (ShellUtils.handleSignalControlCodes(this.commandShell.getParser(), inputValue)) // if input was signaling control code && handled
				{
					switch (inputValue) {
						case TerminalIO.CTRL_C:
							stop = true;
							returnValue = false;
							//termIO.write(BasicTerminalIO.CRLF);
							break;
						case TerminalIO.CTRL_D:  // ctrl+d is catched before this... probably somewhere in telnetd2 library structures, no need for this
							stop = true;
							returnValue = false;
							m_IO.write("BYE");
							break;

					}

					continue;  // continue while cycle
				}

				switch (inputValue) { // HANDLE CONTROL CODES for text manipulation

					case TerminalIO.TABULATOR:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno TABULATOR");
						this.handleTabulator(nalezenePrikazy);
						break;

					case TerminalIO.LEFT:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno LEFT");
						moveCursorLeft(1);
						break;
					case TerminalIO.RIGHT:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno RIGHT");
						moveCursorRight(1);
						break;
					case TerminalIO.UP:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno UP");
						this.handleHistory(TerminalIO.UP);
						break;
					case TerminalIO.DOWN:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno DOWN");
						this.handleHistory(TerminalIO.DOWN);
						break;

					case TerminalIO.DEL:
					case TerminalIO.DELETE:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno DEL/DELETE");
						if (cursor != sb.length()) {
							sb.deleteCharAt(cursor);
							draw();
							Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "DELETE upravil pozici kurzoru na: " + cursor);
						}
						break;

					case TerminalIO.BACKSPACE:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno BACKSPACE");
						if (cursor != 0) {
							sb.deleteCharAt(cursor - 1);
							moveCursorLeft(1);
							draw();
							Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Backspace upravil pozici kurzoru na: " + cursor);
						}
						break;
					case TerminalIO.CTRL_W:
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+W");	// @TODO vyladit smazání slova tak aby odpovídalo konvencím na linuxu
						while (cursor != 0) {
							sb.deleteCharAt(cursor - 1);
							moveCursorLeft(1);
							draw();
							Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "CTRL+W upravil pozici kurzoru na: " + cursor);

							if (cursor != 0 && Character.isSpaceChar(sb.charAt(cursor - 1))) // delete until space is found
							{
								break; // break while
							}

						}
						break; // break switch

					case TerminalIO.CTRL_L:	// clean screen
						Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Přečteno CTRL+L");
						this.clearScreen();
						break;

					case TerminalIO.ENTER:
						stop = true;
						history.add(this.getValue());
						m_IO.write(BasicTerminalIO.CRLF);
						break;

					case -1:
					case -2:
						Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Shell renderer read Input(Code):" + inputValue);
						stop = true;
						break;
				}

				Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "Pozice kurzoru: " + cursor + "Interpretován řídící kod: " + inputValue);


			} catch (IOException ex) {
				stop = true;
				Logger.log(Logger.WARNING, LoggingCategory.TELNET, ex.toString());
				ShellUtils.handleSignalControlCodes(this.commandShell.getParser(), TerminalIO.CTRL_D);  //  CLOSING SESSION SIGNAL
				this.commandShell.quit();
			} catch (UnsupportedOperationException ex) {
				Logger.log(Logger.WARNING, LoggingCategory.TELNET, "Unsuported exception catched in ShellRenderer: " + ex.toString());
			}

		}



	}

	/**
	 * method that return readed line
	 *
	 * @return readed line or null if ctrl+c catched
	 */
	public String getValue() {
		if (returnValue) {
			return this.sb.toString();
		} else {
			return null;
		}
	}

	/**
	 * funkce která překreslí řádek od pozice kurzoru až do jeho konce dle čtecího bufferu
	 *
	 * @throws IOException
	 */
	@Override
	public void draw() throws IOException {

		m_IO.eraseToEndOfScreen();
		m_IO.write(sb.substring(cursor, sb.length()));
		m_IO.moveLeft(sb.length() - cursor);
	}

	/**
	 * překreslí celou řádku, umístí kurzor na konec řádky
	 *
	 * @throws IOException
	 */
	private void drawLine() throws IOException {

		moveCursorLeft(cursor);
		m_IO.eraseToEndOfScreen();
		this.cursor = 0;
		m_IO.write(sb.toString());
		this.cursor = sb.length();

	}

	/**
	 * funkce obsluhující historii, respektive funkce volaná při přečtení kláves UP a DOWN
	 *
	 * @param key typ klávesy který byl přečten
	 * @throws IOException
	 */
	private void handleHistory(int key) throws IOException, TelnetConnectionException {
		if (!(key == TerminalIO.UP || key == TerminalIO.DOWN)) // historie se ovládá pomocí šipek nahoru a dolů, ostatní klávesy ignoruji
		{
			return;
		}

		m_IO.eraseLine();
		m_IO.moveLeft(100);  // kdyby byla lepsi cesta jak smazat řádku, nenašel jsem

		this.commandShell.printPrompt();

		if (key == TerminalIO.UP) {
			//  this.sb.setLength(0);
			this.history.handlePrevious(this.sb);
		} else if (key == TerminalIO.DOWN) {
			//  this.sb.setLength(0);
			this.history.handleNext(this.sb);
		}

		m_IO.write(this.sb.toString());
		m_IO.moveLeft(100);
		m_IO.moveRight(sb.length() + this.commandShell.prompt.length());
		this.cursor = sb.length();

	}

	/**
	 * funkce obstarávající posun kurzoru vlevo. Posouvá "blikající" kurzor, ale i "neviditelný" kurzor značící pracovní
	 * místo v čtecím bufferu
	 */
	private void moveCursorLeft(int times) {

		for (int i = 0; i < times; i++) {
			if (cursor == 0) {
				return;
			} else {
				try {
					m_IO.moveLeft(1);
					cursor--;
				} catch (IOException ex) {
					Logger.log(Logger.WARNING, LoggingCategory.TELNET, ex.toString());

				}


			}
			Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "VLEVO, pozice: " + cursor);

		}

	}

	/**
	 * funkce obstarávající posun kurzoru vpravo. Posouvá "blikající" kurzor, ale i "neviditelný" kurzor značící
	 * pracovní místo v čtecím bufferu
	 */
	private void moveCursorRight(int times) {

		for (int i = 0; i < times; i++) {


			if (cursor >= this.sb.length()) {
				return;
			} else {
				try {
					m_IO.moveRight(1);
					cursor++;
				} catch (IOException ex) {
					Logger.log(Logger.WARNING, LoggingCategory.TELNET, "VPRAVO, pozice: " + cursor);

				}


			}
			Logger.log(Logger.DEBUG, LoggingCategory.TELNET, "VPRAVO, pozice: " + cursor);


		}
	}

	/**
	 *
	 * @param nalezenePrikazy seznam nalezených příkazů z předchozího hledání, pokud prázdný, tak jde o první stisk
	 * tabulatoru
	 */
	private void handleTabulator(List<String> nalezenePrikazy) throws IOException, TelnetConnectionException {

		if (!nalezenePrikazy.isEmpty() && nalezenePrikazy.size() > 1) { // dvakrat zmacknuty tab a mám více než jeden výsledek

			m_IO.write(TerminalIO.CRLF); // nový řádek

			for (String nalezeny : nalezenePrikazy) {
				m_IO.write(nalezeny + "  ");
			}

			m_IO.write(TerminalIO.CRLF); // nový řádek
			this.commandShell.printPrompt();
			m_IO.write(this.sb.toString());


			return;
		}


// nové hledání

		String hledanyPrikaz = this.sb.substring(0, cursor);
//        List<String> prikazy = this.commandShell.getCommandList();
//
//
//        for (String temp : prikazy) {
//            if (temp.startsWith(hledanyPrikaz)) {
//                nalezenePrikazy.add(temp);
//            }
//
//        }
//
//        if (nalezenePrikazy.isEmpty()) // nic jsem nenašel, nic nedělám :)
//        {
//            return;
//        }
//
//
//        if (nalezenePrikazy.size() == 1) // našel jsem jeden odpovídající příkaz tak ho doplním
//        {
//
//            String nalezenyPrikaz = nalezenePrikazy.get(0);
//            String doplnenyPrikaz = nalezenyPrikaz.substring(hledanyPrikaz.length(), nalezenyPrikaz.length());
//
//            sb.insert(cursor, doplnenyPrikaz);
//
//            int tempCursor = cursor;
//            drawLine();
//
//            moveCursorLeft(sb.length() - (tempCursor + doplnenyPrikaz.length()));
//
//        }
//

	}

	private void clearScreen() throws IOException, TelnetConnectionException {
		this.m_IO.eraseScreen();
		m_IO.setCursor(0, 0);
		this.commandShell.printPrompt();
		this.cursor = 0;
		drawLine();

	}
}