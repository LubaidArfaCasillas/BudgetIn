using System;
public class HashTest {
    public static void Main() {
        string[] tests = { "denda", "Denda", "DENDA" };
        foreach (string s in tests) {
            int h = 0;
            foreach (char c in s) {
                h = 31 * h + c;
            }
            int abs = Math.Abs(h);
            int mod = abs % 195;
            float hue = mod < 35 ? mod + 25f : (mod - 35) + 170f;
            Console.WriteLine(s + " -> hash: " + h + ", mod: " + mod + ", hue: " + hue);
        }
    }
}
