pub mod jaso;
pub mod trie;
pub mod model;
pub mod lattice;
pub mod komoran;
pub mod constant;
pub mod combination;

use pyo3::prelude::*;

#[pymodule]
fn komoran_rs(m: &Bound<'_, PyModule>) -> PyResult<()> {
    m.add_class::<komoran::PyKomoran>()?;
    m.add_class::<komoran::PyToken>()?;
    Ok(())
}
