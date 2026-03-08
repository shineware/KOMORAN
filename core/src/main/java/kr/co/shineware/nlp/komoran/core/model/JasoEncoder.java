package kr.co.shineware.nlp.komoran.core.model;

/**
 * Jaso 문자를 compact한 정수(0~66)로 인코딩하는 유틸리티.
 * Double-Array Trie에서 알파벳 크기를 최소화하여 메모리/캐시 효율을 높인다.
 */
public final class JasoEncoder {

    // 초성 19자: ㄱ(0x3131)~ㅎ(0x314E) 중 실제 초성에 해당하는 19자
    // 중성 21자: ㅏ(0x314F)~ㅣ(0x3163)
    // 종성용(호환 자모): 위와 겹치므로 별도 인코딩 불필요 (같은 문자)
    // 기타: 공백, ASCII printable 등

    public static final int ALPHABET_SIZE = 256; // char → int 직접 매핑용 (full range)
    // 실제 trie에서 사용하는 알파벳 크기는 encodedAlphabetSize

    // Direct lookup table: char → encoded int (0-based)
    // -1 means unmapped (will be assigned dynamically)
    private static final int[] ENCODE_TABLE = new int[0x10000]; // full BMP
    private static int nextCode = 1; // 0 is reserved for root

    static {
        java.util.Arrays.fill(ENCODE_TABLE, -1);

        // 호환 자모 영역 (0x3131 ~ 0x3163) - 51자
        for (char c = 0x3131; c <= 0x3163; c++) {
            ENCODE_TABLE[c] = nextCode++;
        }
        // ASCII printable (0x20 ~ 0x7E) - 95자
        for (char c = 0x20; c <= 0x7E; c++) {
            ENCODE_TABLE[c] = nextCode++;
        }
        // 한글 자모 확장 영역 (Hangul Jamo: 0x1100~0x11FF) - 일부만
        for (char c = 0x1100; c <= 0x11FF; c++) {
            ENCODE_TABLE[c] = nextCode++;
        }
    }

    /**
     * 문자를 인코딩된 정수로 변환. 미등록 문자는 동적으로 코드 할당.
     */
    public static int encode(char ch) {
        int code = ENCODE_TABLE[ch];
        if (code == -1) {
            synchronized (JasoEncoder.class) {
                code = ENCODE_TABLE[ch];
                if (code == -1) {
                    code = nextCode++;
                    ENCODE_TABLE[ch] = code;
                }
            }
        }
        return code;
    }

    /**
     * 현재까지 할당된 알파벳 크기 반환.
     */
    public static int getAlphabetSize() {
        return nextCode;
    }

    private JasoEncoder() {}
}
