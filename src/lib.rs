use wasm_bindgen::prelude::*;
use yew::prelude::*;
use yew::format::Json;
use yew::services::{ConsoleService, TimeoutService};
use yew::services::websocket::{WebSocketService, WebSocketStatus, WebSocketTask};
use core::time::Duration;
use yew::services::timeout::TimeoutTask;
use std::rc::Rc;
use serde::{Serialize, Deserialize};
use std::collections::VecDeque;
mod socket;

use socket::Socket;
use chrono::{DateTime, Utc};
use anyhow::Error;


/// Amount of time to wait before attempting reconnect
const RECONNECT_DURATION: Duration = Duration::from_secs(2);

/// UI Socket url
fn ui_socket_url() -> String {
    format!("ws://{}/socket/ui", yew::utils::host().unwrap()).to_string()
}

#[derive(Serialize, Deserialize, Debug)]
pub enum Data {
    Empty,
    Datetime(DateTime<Utc>),
}

struct Model {
    link: ComponentLink<Model>,
    socket: Socket,
    data: VecDeque<Data>,
    server_data: String,
}

#[derive(Debug)]
enum Msg {
    Connect,                                            // connect to websocket server
    Connected,                                          // connected to server
    Disconnected,                                       // disconnected from server
    TextInput(String),                                  // text was input in the input box
    SendText,                                           // send our text to server
    Received(Data),                                     // data received from server
}

impl Component for Model {
    type Message = Msg;
    type Properties = ();

    fn create(_: Self::Properties, link: ComponentLink<Self>) -> Self {
        link.send_message(Msg::Connect);
        Model {
            link,
            socket: Socket::Disconnected,
            data: Default::default(),
            server_data: String::new(),
        }
    }

    fn update(&mut self, msg: Self::Message) -> ShouldRender {
        ConsoleService::log(&format!("update: {:?}", msg));
        match msg {
            Msg::Connect => {
                let cb_data = self.link.callback(|result: Result<Vec<u8>, Error>| {
                    match result {
                        Ok(bytes) => {
                            match bincode::deserialize(&bytes) {
                                Ok(data) => Msg::Received(data),
                                Err(_) => {
                                    ConsoleService::log("deserialization failure, disconnecting");
                                    Msg::Disconnected
                                }
                            }
                        },
                        Err(_) => {
                            ConsoleService::log("socket error, disconnecting");
                            Msg::Disconnected
                        },
                    }
                });
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
                    let socket = WebSocketService::connect_binary(&ui_socket_url(), cb_data, cb_error.into()).unwrap();
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
            Msg::Received(data) => {
                self.data.push_front(data);
                self.data.truncate(10);
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
                <p>{ "Connected: "} { self.socket.is_connected() }</p>
                <p>{ "Received: "}</p>
                { for self.data.iter().map(render_data) }
            </body>
        }
    }
}

fn render_data(data: &Data) -> Html {
    html! {
        <>{ format!("{:?}", data) }<br /></>
    }
}

#[wasm_bindgen(start)]
pub fn run_app() {
    yew::initialize();
    App::<Model>::new().mount_to_body();
}
