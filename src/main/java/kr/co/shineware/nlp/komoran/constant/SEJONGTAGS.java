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

    public static int BOE_ID;

    public static void BOE(int id) {
        BOE_ID = id;
    }

    public static int EOE_ID;

    public static void EOE(int id) {
        EOE_ID = id;
    }

    public static int NA_ID;

    public static void NA(int id) {
        NA_ID = id;
    }

    public static int SN_ID;

    public static void SN(int id) {
        SN_ID = id;
    }

    public static int SW_ID;

    public static void SW(int id) {
        SW_ID = id;
    }

    public static int SH_ID;

    public static void SH(int id) {
        SH_ID = id;
    }

    public static int SL_ID;

    public static void SL(int id) {
        SL_ID = id;
    }

    public static int SO_ID;

    public static void SO(int id) {
        SO_ID = id;
    }

    public static int SE_ID;

    public static void SE(int id) {
        SE_ID = id;
    }

    public static int SS_ID;

    public static void SS(int id) {
        SS_ID = id;
    }

    public static int SP_ID;

    public static void SP(int id) {
        SP_ID = id;
    }

    public static int SF_ID;

    public static void SF(int id) {
        SF_ID = id;
    }

    public static int XR_ID;

    public static void XR(int id) {
        XR_ID = id;
    }

    public static int XSA_ID;

    public static void XSA(int id) {
        XSA_ID = id;
    }

    public static int XSV_ID;

    public static void XSV(int id) {
        XSV_ID = id;
    }

    public static int XSN_ID;

    public static void XSN(int id) {
        XSN_ID = id;
    }

    public static int XPN_ID;

    public static void XPN(int id) {
        XPN_ID = id;
    }

    public static int ETM_ID;

    public static void ETM(int id) {
        ETM_ID = id;
    }

    public static int ETN_ID;

    public static void ETN(int id) {
        ETN_ID = id;
    }

    public static int EC_ID;

    public static void EC(int id) {
        EC_ID = id;
    }

    public static int EF_ID;

    public static void EF(int id) {
        EF_ID = id;
    }

    public static int EP_ID;

    public static void EP(int id) {
        EP_ID = id;
    }

    public static int JC_ID;

    public static void JC(int id) {
        JC_ID = id;
    }

    public static int JX_ID;

    public static void JX(int id) {
        JX_ID = id;
    }

    public static int JKQ_ID;

    public static void JKQ(int id) {
        JKQ_ID = id;
    }

    public static int JKV_ID;

    public static void JKV(int id) {
        JKV_ID = id;
    }

    public static int JKB_ID;

    public static void JKB(int id) {
        JKB_ID = id;
    }

    public static int JKO_ID;

    public static void JKO(int id) {
        JKO_ID = id;
    }

    public static int JKG_ID;

    public static void JKG(int id) {
        JKG_ID = id;
    }

    public static int JKC_ID;

    public static void JKC(int id) {
        JKC_ID = id;
    }

    public static int NNG_ID;

    public static void NNG(int id) {
        NNG_ID = id;
    }

    public static int NNP_ID;

    public static void NNP(int id) {
        NNP_ID = id;
    }

    public static int NNB_ID;

    public static void NNB(int id) {
        NNB_ID = id;
    }

    public static int NP_ID;

    public static void NP(int id) {
        NP_ID = id;
    }

    public static int NR_ID;

    public static void NR(int id) {
        NR_ID = id;
    }

    public static int VV_ID;

    public static void VV(int id) {
        VV_ID = id;
    }

    public static int VA_ID;

    public static void VA(int id) {
        VA_ID = id;
    }

    public static int VX_ID;

    public static void VX(int id) {
        VX_ID = id;
    }

    public static int VCP_ID;

    public static void VCP(int id) {
        VCP_ID = id;
    }

    public static int VCN_ID;

    public static void VCN(int id) {
        VCN_ID = id;
    }

    public static int MM_ID;

    public static void MM(int id) {
        MM_ID = id;
    }

    public static int MAG_ID;

    public static void MAG(int id) {
        MAG_ID = id;
    }

    public static int MAJ_ID;

    public static void MAJ(int id) {
        MAJ_ID = id;
    }

    public static int IC_ID;

    public static void IC(int id) {
        IC_ID = id;
    }

    public static int JKS_ID;

    public static void JKS(int id) {
        JKS_ID = id;
    }

    public static void SET_ID(SEJONGTAGS value, int id) {
        switch (value) {
            case EC:
                EC(id);
                break;
            case EF:
                EF(id);
                break;
            case EP:
                EP(id);
                break;
            case IC:
                IC(id);
                break;
            case JC:
                JC(id);
                break;
            case JX:
                JX(id);
                break;
            case MM:
                MM(id);
                break;
            case NP:
                NP(id);
                break;
            case NR:
                NR(id);
                break;
            case SE:
                SE(id);
                break;
            case SF:
                SF(id);
                break;
            case SH:
                SH(id);
                break;
            case SL:
                SL(id);
                break;
            case SN:
                SN(id);
                break;
            case SO:
                SO(id);
                break;
            case SP:
                SP(id);
                break;
            case SS:
                SS(id);
                break;
            case SW:
                SW(id);
                break;
            case VA:
                VA(id);
                break;
            case VV:
                VV(id);
                break;
            case VX:
                VX(id);
                break;
            case XR:
                XR(id);
                break;
            case ETM:
                ETM(id);
                break;
            case ETN:
                ETN(id);
                break;
            case JKB:
                JKB(id);
                break;
            case JKC:
                JKC(id);
                break;
            case JKG:
                JKG(id);
                break;
            case JKO:
                JKO(id);
                break;
            case JKQ:
                JKQ(id);
                break;
            case JKS:
                JKS(id);
                break;
            case JKV:
                JKV(id);
                break;
            case MAG:
                MAG(id);
                break;
            case MAJ:
                MAJ(id);
                break;
            case NNB:
                NNB(id);
                break;
            case NNG:
                NNG(id);
                break;
            case NNP:
                NNP(id);
                break;
            case VCN:
                VCN(id);
                break;
            case VCP:
                VCP(id);
                break;
            case XPN:
                XPN(id);
                break;
            case XSA:
                XSA(id);
                break;
            case XSN:
                XSN(id);
                break;
            case XSV:
                XSV(id);
                break;
            case NA:
                NA(id);
                break;
            case BOE:
                BOE(id);
                break;
            case EOE:
                EOE(id);
                break;
            default:
                break;
        }
    }
}
