import java.util.*;

class Solution {
    public String reverseStr(String s, int k) {
        char[] chars = s.toCharArray();
        for (int i = 0; i < s.length(); i += 2 * k) {
            if (i + k <= s.length()) {
                reverse(chars, i, i + k);
                continue;
            }
            reverse(chars, i, s.length() - 1);
        }
        return Arrays.toString(chars);
    }

    void reverse(char[] chars, int start, int end) {
        for (int i = start, j = end; i < start - end / 2; i++, j--) {
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
    }
}