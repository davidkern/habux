use anyhow::Result;
use wasm_bindgen::prelude::*;
use yew::prelude::*;
use yew::format::Json;
use yew::services::ConsoleService;
use yew::services::websocket::{WebSocketService, WebSocketStatus, WebSocketTask};

struct Model {
    ws: Option<WebSocketTask>,
    link: ComponentLink<Model>,
    text: String,
    server_data: String,
}

#[derive(Debug)]
enum Msg {
    Connect,                            // connect to websocket server
    Disconnected,                       // disconnected from server
    Ignore,                             // ignore this message
    TextInput(String),                  // text was input in the input box
    SendText,                           // send our text to server
    Received(anyhow::Result<String>),                   // data received from server
}

impl Component for Model {
    type Message = Msg;
    type Properties = ();

    fn create(_: Self::Properties, link: ComponentLink<Self>) -> Self {
        Model {
            ws: None,
            link,
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
                        _ => Msg::Ignore,
                    }
                });
                if self.ws.is_none() {
                    let task = WebSocketService::connect("ws://localhost:8080/ws/", cb_data, cb_error.into()).unwrap();
                    self.ws = Some(task);
                }
                true
            },
            Msg::Disconnected => {
                self.ws = None;
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
            <div>
                <p>{ "Hello, world" }</p>
                <p><button onclick=self.link.callback(|_| Msg::Connect)>{ "Connect" }</button></p><br />
                <p>{ "Connected: "} { !self.ws.is_none() }</p><br />
            </div>
        }
    }
}

#[wasm_bindgen(start)]
pub fn run_app() {
    yew::initialize();
    App::<Model>::new().mount_to_body();
}
