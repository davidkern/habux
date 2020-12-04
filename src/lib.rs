use anyhow::Result;
use wasm_bindgen::prelude::*;
use yew::prelude::*;
use yew::format::Json;
use yew::services::{ConsoleService, TimeoutService};
use yew::services::websocket::{WebSocketService, WebSocketStatus, WebSocketTask};
use core::time::Duration;
use yew::services::timeout::TimeoutTask;
use std::rc::Rc;

/// Amount of time to wait before attempting reconnect
const RECONNECT_DURATION: Duration = Duration::from_secs(2);

/// Maintains state of the websocket
enum Socket {
    Disconnected,
    ReconnectWait(Rc<TimeoutTask>),
    Connecting(Rc<WebSocketTask>),
    Connected(Rc<WebSocketTask>),
}

impl Socket {
    fn is_connected(&self) -> bool {
        match self {
            Self::Connected(_) => true,
            _ => false
        }
    }
}

struct Model {
    link: ComponentLink<Model>,
    socket: Socket,
    text: String,
    server_data: String,
}

#[derive(Debug)]
enum Msg {
    Connect,                                            // connect to websocket server
    Connected,                                          // connected to server
    Disconnected,                                       // disconnected from server
    TextInput(String),                                  // text was input in the input box
    SendText,                                           // send our text to server
    Received(anyhow::Result<String>),                   // data received from server
}

impl Component for Model {
    type Message = Msg;
    type Properties = ();

    fn create(_: Self::Properties, link: ComponentLink<Self>) -> Self {
        link.send_message(Msg::Connect);
        Model {
            link,
            socket: Socket::Disconnected,
            text: String::new(),
            server_data: String::new(),
        }
    }

    fn update(&mut self, msg: Self::Message) -> ShouldRender {
        ConsoleService::log(&format!("update: {:?}", msg));
        match msg {
            Msg::Connect => {
                let cb_data = self.link.callback(|Json(data)| Msg::Received(data));
                let cb_error = self.link.callback(|input| {
                    ConsoleService::log(&format!("Notification: {:?}", input));
                    match input {
                        WebSocketStatus::Closed | WebSocketStatus::Error => {
                            Msg::Disconnected
                        },
                        WebSocketStatus::Opened => {
                            Msg::Connected
                        }
                    }
                });
                if !self.socket.is_connected() {
                    let socket = WebSocketService::connect("ws://localhost:8080/ws/", cb_data, cb_error.into()).unwrap();
                    self.socket = Socket::Connecting(Rc::new(socket));
                }
                false
            },
            Msg::Connected => {
                if let Socket::Connecting(socket) = &self.socket {
                    self.socket = Socket::Connected(Rc::clone(socket));
                }
                true
            },
            Msg::Disconnected => {
                let timer = TimeoutService::spawn(
                    RECONNECT_DURATION,
                    self.link.callback(|_| {
                    Msg::Connect
                }));
                self.socket = Socket::ReconnectWait(Rc::new(timer));
                true
            },
            _ => {
                false
            }
        }
    }

    fn change(&mut self, _: Self::Properties) -> ShouldRender {
        false
    }

    fn view(&self) -> Html {
        html! {
            <body>
                <p>{ "Hello, world" }</p>
                <p>{ "Connected: "} { self.socket.is_connected() }</p><br />
            </body>
        }
    }
}

#[wasm_bindgen(start)]
pub fn run_app() {
    yew::initialize();
    App::<Model>::new().mount_to_body();
}
