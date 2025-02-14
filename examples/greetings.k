/**
 * Expected output:
 *      --- Stefan Student ---
 *      Hi Stefan!
 *      Wie geht's?
 *
 *      --- Dieter Dozent ---
 *      Guten Tag Frau/Herr Dozent.
 *      Wie geht es Ihnen?
 */
fn main() {
    let firstName = "Stefan";
    let lastName = "Student";
    printHeading(firstName, lastName);
    initiateSmallTalk(firstName, lastName, false);

    print("\n");

    firstName = "Dieter";
    lastName = "Dozent";
    printHeading(firstName, lastName);
    initiateSmallTalk(firstName, lastName, true);
}

/**
 * Initiates small talk with the given person in a way depending on whether
 * it's a formal or informal context.
 */
fn initiateSmallTalk(firstName: string, lastName: string, isFormal: bool) {
    greet(firstName, lastName, isFormal);
    howAreYou(isFormal);
}

fn greet(firstName: string, lastName: string, isFormal: bool) {
    let prefix = isFormal ? "Guten Tag Frau/Herr " : "Hi ";
    let suffix = isFormal ? ".\n" : "!\n";
    print(prefix);
    print(isFormal? lastName : firstName);
    print(suffix);
}

fn howAreYou(isFormal: bool) {
    if isFormal {
        print("Wie geht es Ihnen?");
    }
    else {
        print("Wie geht's?");
    }
    print("\n");
}

fn printHeading(firstName: string, lastName: string) {
    print("--- ");
    print(firstName); print(" "); print(lastName);
    print(" ---\n");
}
