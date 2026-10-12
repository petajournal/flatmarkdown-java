use flatmarkdown::{BodyOptions, LinkResolution};
use std::ffi::{CStr, CString};
use std::os::raw::c_char;

/// Link resolution is opt-in since flatmarkdown 0.4.0 (`body_to_ast` alone keeps
/// `#tag` as plain text), so every export enables it with the prefixes
/// PetaJournal writes for page-relative links.
fn body_options() -> BodyOptions {
    BodyOptions {
        link_resolution: LinkResolution::On {
            prefixes: vec!["./".into(), "../".into()],
        },
    }
}

/// Calls `f` with the UTF-8 string behind `input` and returns the result as a
/// heap-allocated C string. The caller must release it with `free_string`.
fn call_str(input: *const c_char, f: fn(&str) -> String) -> *mut c_char {
    let input = unsafe { CStr::from_ptr(input) }.to_str().unwrap();
    CString::new(f(input)).unwrap().into_raw()
}

#[no_mangle]
pub extern "C" fn body_to_html(input: *const c_char) -> *mut c_char {
    call_str(input, |s| flatmarkdown::body_to_html_with(s, &body_options()))
}

#[no_mangle]
pub extern "C" fn body_to_ast(input: *const c_char) -> *mut c_char {
    call_str(input, |s| flatmarkdown::body_to_ast_with(s, &body_options()))
}

#[no_mangle]
pub extern "C" fn page_to_ast(input: *const c_char) -> *mut c_char {
    call_str(input, |s| flatmarkdown::page_to_ast_with(s, &body_options()))
}

#[no_mangle]
pub extern "C" fn free_string(ptr: *mut c_char) {
    if !ptr.is_null() {
        unsafe {
            drop(CString::from_raw(ptr));
        }
    }
}
