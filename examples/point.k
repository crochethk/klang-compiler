struct Point {
    x: i64,
    y: i64,
    ---
    move(dx: i64, dy: i64) -> Point {
        self.x = self.x + dx;
        self.y = self.y + dy;
        return self;
    }

    distanceSquared(other: Point) -> i64 {
        let dx = other.x-self.x;
        let dy = other.y-self.y;
        return dx*dx + dy*dy;
    }
}

fn print_point(p: Point) {
    print("x="); print(p.x); print(", y="); print(p.y);
}

fn ___main___() {
    let p = Point{10, 20};
    p = p.move(5, -5);
    print_point(p); print("\n"); // Expected: 15, 15

    p = p.move(-5,5).move(1,2);
    print_point(p); print("\n"); // Expected: 11, 22

    drop p;


    // Test case 1: Same point (distance should be 0)
    let p1 = Point {0, 0};
    let p2 = Point {0, 0};
    print(p1.distanceSquared(p2));  // Expected: 0
    print("\n");

    // Test case 2: Horizontal distanceSquared
    let p3 = Point {3, 0};
    print(p1.distanceSquared(p3));  // Expected: 9
    print("\n");

    // Test case 3: Vertical distanceSquared
    let p4 = Point {0, 4};
    print(p1.distanceSquared(p4));  // Expected: 16
    print("\n");

    // Test case 4: Diagonal distanceSquared
    let p5 = Point {3, 4};
    print(p1.distanceSquared(p5));  // Expected: 25
    print("\n");

    // Test case 5: Negative coordinates
    let p6 = Point {-1, -1};
    print(p1.distanceSquared(p6));  // Expected: 2
    print("\n");

    drop p1;
    drop p2;
    drop p3;
    drop p4;
    drop p5;
    drop p6;
}

