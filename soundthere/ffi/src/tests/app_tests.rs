//! SoundCraft's own: a session starts with a project, and the demo project has tracks.

use super::*;

#[test]
fn a_new_session_describes_its_project() {
    let s = loki_session_new();
    let r = call(s, "session.inspect", None);
    assert_eq!(r["ok"], true, "{r}");
    assert!(r["result"].is_object(), "{r}");
    assert_eq!(call(s, "loki.surface", None)["result"]["kind"], "timeline");
    unsafe { loki_session_free(s) };
}

#[test]
fn the_demo_project_has_tracks() {
    let s = loki_session_new();
    assert_eq!(call(s, "loki.demo", None)["ok"], true);
    let r = call(s, "session.inspect", None)["result"].clone();
    assert!(r["tracks"].as_array().is_some_and(|t| !t.is_empty()), "{r}");
    unsafe { loki_session_free(s) };
}
