package treasurehunt.util;

/**
 * A convenient utility that allows for debug information to be printed only
 * when debug mode is switched on using Debug.on(). Debug mode can be switched
 * off at any time using Debug.off().
 *
 * Debug provides three primary ways to print information. A message, a warnining,
 * and an error. Each method will automatically change the given message to
 * reflect the type of method chosen.
 */
public class Debug
{
    private static boolean on = false;

    public static void on()
    {
        on = true;
        message("Debug ON");
    }

    public static void off()
    {
        message("Debug OFF");
        on = false;
    }

    public boolean isOn() { return on; }
    public boolean isOff() { return !on; }

    public static void print(String message) { if(on) System.out.print(message); }
    public static void println(String message) { if(on) System.out.println(message); }

    public static void out(String message) { if(on) System.out.println("[DEBUG] " + message); }

    public static void message(String message) { out("[MSG] " + message); }
    public static void msg(String message) { message(message); }

    public static void warning(String warning) { out("[WRN] " + warning); }
    public static void wrn(String warning) { warning(warning); }

    public static void error(String error) { out("[ERR] " + error); }
    public static void err(String error) { error(error); }
}
