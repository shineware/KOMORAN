/// KOMORAN 상수 정의

// 특수 심볼
pub const BOE: &str = "BOE";
pub const EOE: &str = "EOE";
pub const NA: &str = "NA";
pub const SPACE: &str = "<sp>";
pub const NUMBER: &str = "<number>";
pub const IRREGULAR: &str = "IRR";
pub const IRREGULAR_ID: i32 = -1;

// 품사 태그
pub const NNG: &str = "NNG";
pub const NNP: &str = "NNP";
pub const NNB: &str = "NNB";
pub const NP: &str = "NP";
pub const NR: &str = "NR";
pub const VV: &str = "VV";
pub const VA: &str = "VA";
pub const VX: &str = "VX";
pub const VCP: &str = "VCP";
pub const VCN: &str = "VCN";
pub const SW: &str = "SW";
pub const SF: &str = "SF";
pub const EC: &str = "EC";
pub const EF: &str = "EF";
pub const JKO: &str = "JKO";
pub const JX: &str = "JX";
pub const ETM: &str = "ETM";
pub const ETN: &str = "ETN";
pub const JKS: &str = "JKS";
pub const JKC: &str = "JKC";
pub const JKB: &str = "JKB";
pub const JKV: &str = "JKV";
pub const JKG: &str = "JKG";
pub const JC: &str = "JC";
pub const SS: &str = "SS";
pub const EP: &str = "EP";
pub const SL: &str = "SL";
pub const SN: &str = "SN";
pub const SH: &str = "SH";
pub const SO: &str = "SO";
pub const SE: &str = "SE";
pub const SP: &str = "SP";

// 점수 상수
pub const SCORE_NA: f64 = -10000.0;
pub const SCORE_SL: f64 = -1.0;
pub const SCORE_SN: f64 = -1.0;
pub const SCORE_SH: f64 = -1.0;
pub const SCORE_SF: f64 = -1.0;
pub const SCORE_SW: f64 = -10000.0;
pub const SCORE_SP: f64 = -1.0;
pub const SCORE_SS: f64 = -1.0;
pub const SCORE_SO: f64 = -1.0;

/// 명사류 품사 집합
pub const NOUN_TAGS: &[&str] = &[NNG, NNP, NNB, NP, NR];
/// 어미류 품사 집합
pub const EOMI_TAGS: &[&str] = &[EP, EC, EF, ETN, ETM];
/// 조사류 품사 집합
pub const JOSA_TAGS: &[&str] = &[JC, JKB, JKC, JKG, JKO, JKS, JKV, JX];

/// 세종 품사 태그 ID 관리
#[derive(Clone, Debug)]
pub struct SejongTags {
    pub boe_id: i32,
    pub eoe_id: i32,
    pub na_id: i32,
    pub sn_id: i32,
    pub sw_id: i32,
    pub sh_id: i32,
    pub sl_id: i32,
    pub so_id: i32,
    pub se_id: i32,
    pub ss_id: i32,
    pub sp_id: i32,
    pub sf_id: i32,
    pub ec_id: i32,
    pub ef_id: i32,
    pub etm_id: i32,
    pub etn_id: i32,
    pub ep_id: i32,
    pub jks_id: i32,
    pub jkc_id: i32,
    pub jko_id: i32,
    pub jkv_id: i32,
    pub jkb_id: i32,
    pub jx_id: i32,
    pub jc_id: i32,
    pub vv_id: i32,
    pub nng_id: i32,
    pub nnp_id: i32,
    pub nnb_id: i32,
    pub np_id: i32,
    pub nr_id: i32,
}

impl SejongTags {
    pub fn new() -> Self {
        SejongTags {
            boe_id: -1, eoe_id: -1, na_id: -1, sn_id: -1, sw_id: -1,
            sh_id: -1, sl_id: -1, so_id: -1, se_id: -1, ss_id: -1,
            sp_id: -1, sf_id: -1, ec_id: -1, ef_id: -1, etm_id: -1,
            etn_id: -1, ep_id: -1, jks_id: -1, jkc_id: -1, jko_id: -1,
            jkv_id: -1, jkb_id: -1, jx_id: -1, jc_id: -1, vv_id: -1,
            nng_id: -1, nnp_id: -1, nnb_id: -1, np_id: -1, nr_id: -1,
        }
    }

    pub fn build_from_pos_table(&mut self, pos_to_id: &std::collections::HashMap<String, i32>) {
        let get = |name: &str| -> i32 { *pos_to_id.get(name).unwrap_or(&-1) };
        self.boe_id = get("BOE");
        self.eoe_id = get("EOE");
        self.na_id = get("NA");
        self.sn_id = get("SN");
        self.sw_id = get("SW");
        self.sh_id = get("SH");
        self.sl_id = get("SL");
        self.so_id = get("SO");
        self.se_id = get("SE");
        self.ss_id = get("SS");
        self.sp_id = get("SP");
        self.sf_id = get("SF");
        self.ec_id = get("EC");
        self.ef_id = get("EF");
        self.etm_id = get("ETM");
        self.etn_id = get("ETN");
        self.ep_id = get("EP");
        self.jks_id = get("JKS");
        self.jkc_id = get("JKC");
        self.jko_id = get("JKO");
        self.jkv_id = get("JKV");
        self.jkb_id = get("JKB");
        self.jx_id = get("JX");
        self.jc_id = get("JC");
        self.vv_id = get("VV");
        self.nng_id = get("NNG");
        self.nnp_id = get("NNP");
        self.nnb_id = get("NNB");
        self.np_id = get("NP");
        self.nr_id = get("NR");
    }
}
