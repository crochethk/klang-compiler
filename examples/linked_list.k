
/* The good old, singly linked list. */
struct LLi64 {
    value: i64,
    next: LLi64,
    ---
    /* Appends the given value to the list and returns the appended node. */
    append(newValue: i64) -> LLi64 {
        if self.next == null {
            self.next = LLi64{newValue, null};
            return self.next;
        }
        return self.next.append(newValue);
    }

    /* Drop the list and all its nodes. */
    dispose() {
        let _next = self.next;
        self.next = null;
        if _next != null {
            _next.dispose();
        }
        drop self;
    }

    sum() -> i64 {
        if (self.next == null) {
            return self.value;
        }
        return self.value + self.next.sum();
    }
}

fn ___main___() {
    let _: LLi64; // dummy variable

    // 1: Single node operations
    let list1 = LLi64{1, null};
    print(list1.sum());  // Expected: 1
    print("\n");
    list1.dispose();

    // 2: Building list with append
    let list2 = LLi64{10, null};
    _ = list2.append(20).append(30).append(40);
    print(list2.sum());  // Expected: 100
    print("\n");
    list2.dispose();

    // 3: Zero and negative values
    let list3 = LLi64{0, null};
    _ = list3.append(-5).append(5).append(-10);
    print(list3.sum());  // Expected: -10
    print("\n");
    list3.dispose();

    // 4: Large numbers
    let list4 = LLi64{1000000, null};
    _ = list4.append(2000000).append(3000000);
    print(list4.sum());  // Expected: 6000000
    print("\n");
    list4.dispose();

    // 5: Single value repeated
    let list5 = LLi64{1, null};
    _ = list5.append(1).append(1).append(1).append(1);
    print(list5.sum());  // Expected: 5
    print("\n");
    list5.dispose();
}
