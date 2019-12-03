#[macro_use]
extern crate riup;
use riup::*;

extern fn bt_click(w:IUPPtr,_:CBPtr,_:CBPtr,_:CBPtr)->i32{
    Text::from_ptr(get_handle("text")).set_text("Hello world");
    0
}

fn main() {
    init_gui();

    let bt = Button::new(" Button ").set_width(100).on_click(bt_click);
    let txt = Text::new().set_width(100).set_text("Hello").handle("text");
    let hbox = Hbox::new(vec_ptr!(bt, txt)).gap(5).margin("5x15");
    let dlg = Dialog::new(hbox.ptr()).set_text("RIup").set_size("250x100");
    show(dlg.ptr());

    loop_gui();

}
