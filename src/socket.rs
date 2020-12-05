use std::rc::Rc;
use yew::services::timeout::TimeoutTask;
use yew::services::websocket::WebSocketTask;

/// Maintains state of the websocket
pub enum Socket {
    Disconnected,
    ReconnectWait(Rc<TimeoutTask>),
    Connecting(Rc<WebSocketTask>),
    Connected(Rc<WebSocketTask>),
}

impl Socket {
    pub fn is_connected(&self) -> bool {
        match self {
            Self::Connected(_) => true,
            _ => false
        }
    }
}
