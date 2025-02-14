fn get_grade(score: i64) -> string {
    if score < 0 || score > 100 {
        return "Invalid score";
    }

    return score >= 90 ? "Note 1" :
           score >= 75 ? "Note 2" :
           score >= 60 ? "Note 3" :
           score >= 50 ? "Note 4" : "Note 5";
}

fn ___main___() {
    let score: i64;

    score = 99;
    print(get_grade(score)); print("\n"); // Expected: "Note 1"
    score = 59;
    print(get_grade(score)); print("\n"); // Expected: "Note 4"
    score = 89;
    print(get_grade(score)); print("\n"); // Expected: "Note 2"
    score = 74;
    print(get_grade(score)); print("\n"); // Expected: "Note 3"
    score = 49;
    print(get_grade(score)); print("\n"); // Expected: "Note 5"
}
