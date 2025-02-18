/**
 * Approximates `sin(_x)` using taylor expansion (for `0≤x≤pi/2`)
 * and exploiting symmetry properties.
 */
fn sin_taylor(_x: f64) -> f64 {
    let PI: f64 = 3.141592653589793;
    let PI_PI: f64 = 2.0 * PI;
    let HALF_PI: f64 = 0.5 * PI;

    let x = _x;

    // Factor to multiply final approximation
    let symmetry_fac = 1.0;


    // Handle negative _x
    if x<0.0 {
        //4) Apply symmetry "sin(-x) = -sin(x)"
        x = -x;
        symmetry_fac = -symmetry_fac;
    }

    if x > PI_PI {
        //1) normalize |_x| to 0 ≤ x ≤ 2PI
        let periods = x / PI_PI;
        let per_rem = periods - (periods as i64) as f64;
        x = PI_PI * per_rem;
    }

    if PI <= x && x <= PI_PI {
        //2) normalize pi ≤ x ≤ 2pi to 0 ≤ x < PI
        symmetry_fac = -symmetry_fac;
        x = x-PI;
    }

    if HALF_PI <= x && x < PI {
        //3) normalize x to 0 ≤ x < pi/2
        // apply symmetry:
        //      x = pi/2 + dx 
        //      ---> dx = x - pi/2
        //      sin(pi/2 + dx) = sin(pi/2 - dx)
        //      ---> sin(x) = sin(pi/2 - (x-pi/2)) = sin(pi - x)
        x = PI-x;
    }

    // Taylor expansion for 0 ≤ x ≤ pi/2
    let c1: f64 = 1.0/(2.0*3.0);
    let c2: f64 = c1/(4.0*5.0);
    let c3: f64 = c2/(6.0*7.0);
    let x2 = x*x;
    let x3 = x2*x;
    let x5 = x2*x3;
    let x7 = x2*x5;
    let approx = x-x3*c1 + x5*c2 - x7*c3;

    return symmetry_fac * approx;
}

fn main() {
    let x: f64;

    x = 1.0;
    print("x="); print(x); print("\n");
    print("Taylor expansion of sin(x): "); print(sin_taylor(x)); print("\n");
    print("Reference sin(x): 0.8414709848078965"); print("\n\n");

    x = -1.0;
    print("x="); print(x); print("\n");
    print("Taylor expansion of sin(x): "); print(sin_taylor(x)); print("\n");
    print("Reference sin(x): -0.8414709848078965"); print("\n\n");

    x = 3.0;
    print("x="); print(x); print("\n");
    print("Taylor expansion of sin(x): "); print(sin_taylor(x)); print("\n");
    print("Reference sin(x): 0.1411200080598672"); print("\n\n");

    x = -3.0;
    print("x="); print(x); print("\n");
    print("Taylor expansion of sin(x): "); print(sin_taylor(x)); print("\n");
    print("Reference sin(x): -0.1411200080598672"); print("\n\n");

    x = 3.5;
    print("x="); print(x); print("\n");
    print("Taylor expansion of sin(x): "); print(sin_taylor(x)); print("\n");
    print("Reference sin(x): -0.35078322768961984"); print("\n\n");

    x = -3.5;
    print("x="); print(x); print("\n");
    print("Taylor expansion of sin(x): "); print(sin_taylor(x)); print("\n");
    print("Reference sin(x): 0.35078322768961984"); print("\n\n");

    x = 4.9;
    print("x="); print(x); print("\n");
    print("Taylor expansion of sin(x): "); print(sin_taylor(x)); print("\n");
    print("Reference sin(x): -0.9824526126243325"); print("\n\n");

    x = -4.9;
    print("x="); print(x); print("\n");
    print("Taylor expansion of sin(x): "); print(sin_taylor(x)); print("\n");
    print("Reference sin(x): 0.9824526126243325"); print("\n\n");

    x = 1234.0;
    print("x="); print(x); print("\n");
    print("Taylor expansion of sin(x): "); print(sin_taylor(x)); print("\n");
    print("Reference sin(x): 0.6019276547624973"); print("\n\n");

    x = -1234.0;
    print("x="); print(x); print("\n");
    print("Taylor expansion of sin(x): "); print(sin_taylor(x)); print("\n");
    print("Reference sin(x): -0.6019276547624973"); print("\n\n");
}
