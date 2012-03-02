package me.rafa652.minecraftbot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jibble.pircbot.Colors;

/**
 * Representation of a color code
 * @author Rafa652
 */
public enum Color {
	// IRC values based on mIRC's documentation
	// http://www.mirc.com/colors.html
	// I used the closest color possible for Minecraft
	WHITE       ("\u000300", "�f"),
	BLACK       ("\u000301", "�0"),
	DARK_BLUE   ("\u000302", "�1"),
	BLUE        ("\u000302", "�1"), // duplicate
	GREEN       ("\u000303", "�2"),
	RED         ("\u000304", "�c"),
	BROWN       ("\u000305", "�4"), // using MC dark red
	PURPLE      ("\u000306", "�5"),
	ORANGE      ("\u000307", "�6"), // using MC gold
	YELLOW      ("\u000308", "�e"),
	LIGHT_GREEN ("\u000309", "�a"),
	TEAL        ("\u000310", "�b"), // Using MC aqua
	AQUA        ("\u000310", "�b"), // duplicate
	LIGHT_CYAN  ("\u000311", "�b"), // using MC aqua
	CYAN        ("\u000311", "�b"), // duplicate
	LIGHT_BLUE  ("\u000312", "�9"),
	PINK        ("\u000313", "�d"),
	GRAY        ("\u000314", "�7"),
	GREY        ("\u000314", "�7"), // duplicate
	LIGHT_GRAY  ("\u000315", "�8"),
	LIGHT_GREY  ("\u000315", "�8"), // duplicate
	RESET       ("\u0003", "�f"),   // color code on its own usually means the color ends here  
	NORMAL      ("\u000f", "�f");   // There is no "normal" code on MC, so using white.
	
	public final String irc; // IRC control code and color value
	public final String mc; // Minecraft two-character color code

	private Color(String irc, String mc) {
		this.irc = irc;
		this.mc = mc;
	}
	
	public static String toIRC(final String line) {
		String msg = new String(line);
		for (Color c : Color.values())
			msg = msg.replaceAll(c.mc, c.irc);
		return msg + "\u000f"; // Colors shouldn't "leak" into the rest of the string
	}
	public static String toMC(final String line) {
		String msg = fix(line);
		for (Color c : Color.values())
			msg = msg.replaceAll(c.irc, c.mc);
		return Colors.removeFormattingAndColors(msg); // and finally get rid of everything else
	}
	
	private static String fix(String line) {
		// Catching background colors
		Pattern pattern = Pattern.compile("\u0003[0-9]{1,2}(,[0-9]{1,2})?");
		Matcher matcher = pattern.matcher(line);
		while (matcher.find()) {
			CharSequence bg = matcher.group(1); // This returns null if group 1 doesn't exist
			if (bg != null) line = line.replace(bg, "");
		}
		
		// This picks apart the string character by character as it looks to see if
		// there was a one digit color code entered instead of a two-digit one, then fixes it.
		// I spent three hours looking for an alternative. Regular expressions don't seen to work.
		// Code from other IRC clients that deal with this are almost unreadable or too complicated
		// to just copy over.

		// Consider that it took three hours until I gave up trying to look for an "easy way"
		// to do this and it took me 15 minutes to figure out how to do it the "hard way"... 
		boolean found = false;
		int i=-1;
		do {
			i++;
			char cl = line.charAt(i);
			if (cl == '\u0003') {
				found = true; // found color code
				continue;
			}
			if (found && Character.isDigit(cl)) { // digit character found after code
				if (cl == '1' || cl == '0') { // it's 0 or 1 - must check if there's another number after this
					i++; if (i>line.length()-1) break; cl = line.charAt(i); // get next character
					if (!Character.isDigit(cl)) { // this next one is not a number
						//insert
						StringBuffer sb1 = new StringBuffer(line);
						sb1.insert(i-1, '0'); //adding -before-, not -at- current position
						line = sb1.toString();
						i++;
					}
				}
				else { // not 0 or 1 - definitely adding a 0 in front
					//insert
					StringBuffer sb1 = new StringBuffer(line);
					sb1.insert(i, '0'); // adding 0 at the current position
					line = sb1.toString();
					i++;
				}
				
				found = false;
			}
		} while (i<line.length()-1);
		
		return line;
	}
}