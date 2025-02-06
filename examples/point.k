struct Point {
    x: i64,
    y: i64,
    ---
    move(dx: i64, dy: i64) -> Point {
        self.x = self.x + dx;
        self.y = self.y + dy;
        return self;
    }
}

fn create_point(x: i64, y: i64) -> Point {
    return Point { x, y };
}

fn main() {
    let p = create_point(10, 20);
    p = p.move(5, -5);
    print("x: "); print(p.x); print("\n"); // ->15
    print("y: "); print(p.y); print("\n"); // ->15

    p = p.move(-5,5).move(1,2);
    print("x: "); print(p.x); print("\n"); // ->11
    print("y: "); print(p.y); print("\n"); // ->22

    drop p;
}

