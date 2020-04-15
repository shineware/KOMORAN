package kr.co.shineware.nlp.komoran.core.model;

public class MorphUtil {

    public MorphUtil() {
        ;
    }

    public boolean isSameJaso(String sourceMorph, String compareMorphJaso) {
        if (sourceMorph.length() == compareMorphJaso.length()) {
            for (int i = 0; i < compareMorphJaso.length(); i++) {
                if (sourceMorph.charAt(i) != compareMorphJaso.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean hasJongsung(String morph) {
        char prevLastJaso = morph.charAt(morph.length() - 1);
        if (0x3131 <= prevLastJaso && prevLastJaso <= 0x314e) {
            return prevLastJaso != 0x3138 && prevLastJaso != 0x3143 && prevLastJaso != 0x3149;
        }
        return false;
    }
}
