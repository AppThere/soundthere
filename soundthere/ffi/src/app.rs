//! What is SoundCraft's own: the session and how methods reach it. The arrangement is drawn by the
//! front end from `session.inspect`, so there is no bitmap surface.

use serde_json::{Value, json};
use soundcraft_automation::{Backend, Headless};
use soundcraft_engine::Engine;

use crate::Rendered;

pub(crate) struct Inner {
    h: Headless,
}

/// A session with a new, empty project. The person runs the app on their own machine, so file access
/// is not confined to roots.
pub(crate) fn new() -> Inner {
    Inner { h: Headless::new(Engine::default()) }
}

/// Every control-protocol method the headless backend answers (`engine.execute`, `engine.commands`,
/// `session.inspect`, …), and `loki.demo`, which replaces the project with the engine's demo one.
pub(crate) fn call(inner: &mut Inner, method: &str, params: Value) -> Result<Value, String> {
    if method == "loki.demo" {
        inner.h = Headless::new(soundcraft_engine::demo::demo_engine());
        return Ok(json!({"ok": true}));
    }
    // `session.inspect` is a control-protocol method, not a command; the front end reads it as a state
    // command, so `engine.execute` reaches it too and the listing names it.
    if method == "engine.execute" && params.get("command").and_then(Value::as_str) == Some("session.inspect") {
        return inner.h.call("session.inspect", params.get("params").cloned().unwrap_or_else(|| json!({})));
    }
    if method == "engine.commands" {
        let mut list = inner.h.call(method, params)?;
        if let Some(a) = list.as_array_mut().filter(|a| !a.iter().any(|c| c.get("id").and_then(Value::as_str) == Some("session.inspect"))) {
            a.push(json!({"id": "session.inspect", "label": "Inspect Session", "menu": [], "shortcut": null, "params": "{detail?: full}",
                "undoable": false, "journal": false}));
        }
        return Ok(list);
    }
    inner.h.call(method, params)
}

/// Nothing is queued for the front end yet (the playbook's phase 6).
pub(crate) fn ui_requests(_inner: &mut Inner) -> Value {
    json!([])
}

/// `timeline`: drawn by the front end from `session.inspect`; the engine has no picture of it.
pub(crate) fn surface(_inner: &mut Inner) -> Value {
    json!({"kind": "timeline", "pages": 0, "current": 0})
}

pub(crate) fn render(_inner: &mut Inner, _page: u32, _scale: f32, _max_pixels: u64) -> Option<Rendered> {
    None
}
