package kr.co.shineware.nlp.komoran.constant;

public enum SEJONGTAGS {
    NNG, NNP, NNB,
    NP,
    NR,
    VV,
    VA,
    VX,
    VCP, VCN,
    MM,
    MAG, MAJ,
    IC,
    JKS, JKC, JKG, JKO, JKB, JKV, JKQ,
    JX,
    JC,
    EP, EF, EC, ETN, ETM,
    XPN,
    XSN, XSV, XSA,
    XR,
    SF, SP, SS, SE, SO, SL, SH, SW, SN,
    NA,
    //Begin Of Eojeol
    BOE,
    //End Of Eojeol
    EOE;

    private static final int[] TAG_IDS = new int[values().length];
    private static volatile boolean initialized = false;

    static {
        java.util.Arrays.fill(TAG_IDS, -1);
    }

    public int getId() {
        return TAG_IDS[this.ordinal()];
    }

    public static void SET_ID(SEJONGTAGS value, int id) {
        TAG_IDS[value.ordinal()] = id;
        initialized = true;
    }

    // Cached ID fields for hot-path access (read from array, no volatile per field)
    public static int BOE_ID;
    public static int EOE_ID;
    public static int NA_ID;
    public static int SN_ID;
    public static int SW_ID;
    public static int SH_ID;
    public static int SL_ID;
    public static int SO_ID;
    public static int SE_ID;
    public static int SS_ID;
    public static int SP_ID;
    public static int SF_ID;
    public static int XR_ID;
    public static int XSA_ID;
    public static int XSV_ID;
    public static int XSN_ID;
    public static int XPN_ID;
    public static int ETM_ID;
    public static int ETN_ID;
    public static int EC_ID;
    public static int EF_ID;
    public static int EP_ID;
    public static int JC_ID;
    public static int JX_ID;
    public static int JKQ_ID;
    public static int JKV_ID;
    public static int JKB_ID;
    public static int JKO_ID;
    public static int JKG_ID;
    public static int JKC_ID;
    public static int NNG_ID;
    public static int NNP_ID;
    public static int NNB_ID;
    public static int NP_ID;
    public static int NR_ID;
    public static int VV_ID;
    public static int VA_ID;
    public static int VX_ID;
    public static int VCP_ID;
    public static int VCN_ID;
    public static int MM_ID;
    public static int MAG_ID;
    public static int MAJ_ID;
    public static int IC_ID;
    public static int JKS_ID;

    public static void publishIds() {
        BOE_ID = TAG_IDS[BOE.ordinal()];
        EOE_ID = TAG_IDS[EOE.ordinal()];
        NA_ID = TAG_IDS[NA.ordinal()];
        SN_ID = TAG_IDS[SN.ordinal()];
        SW_ID = TAG_IDS[SW.ordinal()];
        SH_ID = TAG_IDS[SH.ordinal()];
        SL_ID = TAG_IDS[SL.ordinal()];
        SO_ID = TAG_IDS[SO.ordinal()];
        SE_ID = TAG_IDS[SE.ordinal()];
        SS_ID = TAG_IDS[SS.ordinal()];
        SP_ID = TAG_IDS[SP.ordinal()];
        SF_ID = TAG_IDS[SF.ordinal()];
        XR_ID = TAG_IDS[XR.ordinal()];
        XSA_ID = TAG_IDS[XSA.ordinal()];
        XSV_ID = TAG_IDS[XSV.ordinal()];
        XSN_ID = TAG_IDS[XSN.ordinal()];
        XPN_ID = TAG_IDS[XPN.ordinal()];
        ETM_ID = TAG_IDS[ETM.ordinal()];
        ETN_ID = TAG_IDS[ETN.ordinal()];
        EC_ID = TAG_IDS[EC.ordinal()];
        EF_ID = TAG_IDS[EF.ordinal()];
        EP_ID = TAG_IDS[EP.ordinal()];
        JC_ID = TAG_IDS[JC.ordinal()];
        JX_ID = TAG_IDS[JX.ordinal()];
        JKQ_ID = TAG_IDS[JKQ.ordinal()];
        JKV_ID = TAG_IDS[JKV.ordinal()];
        JKB_ID = TAG_IDS[JKB.ordinal()];
        JKO_ID = TAG_IDS[JKO.ordinal()];
        JKG_ID = TAG_IDS[JKG.ordinal()];
        JKC_ID = TAG_IDS[JKC.ordinal()];
        NNG_ID = TAG_IDS[NNG.ordinal()];
        NNP_ID = TAG_IDS[NNP.ordinal()];
        NNB_ID = TAG_IDS[NNB.ordinal()];
        NP_ID = TAG_IDS[NP.ordinal()];
        NR_ID = TAG_IDS[NR.ordinal()];
        VV_ID = TAG_IDS[VV.ordinal()];
        VA_ID = TAG_IDS[VA.ordinal()];
        VX_ID = TAG_IDS[VX.ordinal()];
        VCP_ID = TAG_IDS[VCP.ordinal()];
        VCN_ID = TAG_IDS[VCN.ordinal()];
        MM_ID = TAG_IDS[MM.ordinal()];
        MAG_ID = TAG_IDS[MAG.ordinal()];
        MAJ_ID = TAG_IDS[MAJ.ordinal()];
        IC_ID = TAG_IDS[IC.ordinal()];
        JKS_ID = TAG_IDS[JKS.ordinal()];
    }
}
