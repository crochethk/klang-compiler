<!-- This readme was created based on https://github.com/othneildrew/Best-README-Template -->
<a id="readme-top"></a>

<h3 align="center">The Klang Compiler</h3>

  <p align="center">
    A compiler for <a href="the-klang-language.md">the Klang programming language</a>.
  </p>
</div>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
    </li>
    <li>
      <a href="#how-it-works">How it works</a>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#quick-start">Quick Start</a></li>
        <li><a href="#building-and-using-self-contained-jar">Building And Using Self Contained Jar</a></li>
      </ul>
    </li>
    <!-- <li><a href="#usage">Usage</a></li> -->
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#license">License</a></li>
    <!-- <li><a href="#contact">Contact</a></li> -->
  </ol>
</details>


<!-- ABOUT THE PROJECT -->
## About The Project

This project is a compiler for the "Klang" programming language, developed as 
part of a Compiler Construction course.
The compiler's main focus is to produce linux compatible GNU Assembly Code from 
a given Klang source code file. It also is capable of generating Java Byte Code 
(JBC) – though the supported language features are very limited at the moment. 

The language's syntax is slightly inspired by [Rust](https://www.rust-lang.org) 
and a program may look something like the following screenshot:

![Example Klang Program][product-screenshot]

You can find out more about the structure and how to write a Klang program 
[here](the-klang-language.md). Also there is an [`examples`](examples/) folder
with some basic programs.

### How it works
The language is defined using ANTLR 4 grammar which is then used to generate
an according Lexer and Parser. The latter creates a Parse Tree which is
then transformed into an *Abstract Syntax Tree* (AST). The AST consists of Nodes
implementing the *Visitor Pattern* which are traversed by several visitors, each
performing the respective task on each Node of the tree.
In summary there is a PrettyPrinter, a TypeChecker and several Code Generator
visitors.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- GETTING STARTED -->
## Getting Started
The following steps will guide you through the process of compiling a Klang source
code file.

### Prerequisites
* [![Java][Java]][Java-url] ≥23
* `gcc` to compile generated helpers and link with assembly code
* WSL or Linux 
    > Provided bash scripts and the generated assembly only support Linux

### Quick Start
The easiest way to start compiling Klang files is to use the provided bash
script `scripts/compile_klang.sh`.
To demonstrate this the following steps will show how to compile the file
`examples/point.k`.

1. If not done yet, get a local copy of this repo
    ```sh
    git clone https://github.com/crochethk/klang-compiler.git
    cd klang-compiler
    ```

2. Run the script
    ```sh
    ./scripts/compile_klang.sh asm "./out" "examples/point.k"
    ```
    This will create a subdirectory `./out` with 3 files:
    - `examples.point.s` - The main assembly code.
    - `examples.point.h` - Header with definitions for integration with C code.
    - `examples.point.c` - Helper C code required for some language features.

    > The `examples` part in the file names is the package inferred by the
        compiler based on the relative path of the provided Klang file.
        Thus it's important to **first navigate to** your source code's **root** 
        folder before compiling.

3. Link and run the program
    ```
    cd out
    gcc -Wall examples.point.s examples.point.c
    ./a.out
    ```
<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Building And Using Self Contained `jar`
You can also package the compiler into a self contained `jar` using a provided
script.
1. If not done yet, get a local copy of this repo
    ```sh
    git clone https://github.com/crochethk/klang-compiler.git
    cd klang-compiler
    ```

2. Run the bash script (this may take ~30 sec)
    ```sh
    ./scripts/build_compiler_jar.sh
    ```
    This will create a subdirectory `build` with two relevant files:
    - `klangc.jar` - The actual compiler executable.
    - `klangc.sh` - A convenience script to run `jar`.

3. Compile a Klang file

    To compile the example source code `examples/point.k` follow these steps:
    - Run
        ```sh
        ./build/klangc.sh examples/point.k
        ```
The output and the other steps are similar to [Quick Start](#quick-start).

To print further usage info or inspect all available options run
```sh
./build/klangc.sh --help
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>




<!-- ROADMAP -->
## Roadmap

- [x] Add support for casting of numerical types
- [ ] Full JBC support
- [ ] Add array types
- [ ] Extend string manipulation support
    - [ ] Length
    - [ ] Concatenation (`strA + strB`)
    - [ ] Substring
- [ ] Add support for builtin struct methods (e.g. `to_string()`)
- [ ] VSCode Syntax Highlight Extension


<p align="right">(<a href="#readme-top">back to top</a>)</p>




<!-- LICENSE -->
## License

Except for the following third-party libraries, which are redistributed under 
their respective licenses, all code in this repository is licensed under the
[MIT License](LICENSE).

The following libraries are included in this repository and are redistributed 
under their respective open-source licenses:

1. **ANTLR4 Runtime** and **ANTLR4 Complete** are licensed under the 
[BSD License](https://opensource.org/licenses/BSD-3-Clause).
    The full license text can be found in [`antlr4-LICENSE.txt`](lib/antlr4-LICENSE.txt).

2. **JUnit Platform Console Standalone** is licensed under the 
[Eclipse Public License 2.0](https://www.eclipse.org/legal/epl-2.0/).


<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/crochethk/klang-compiler.svg?style=for-the-badge
[contributors-url]: https://github.com/crochethk/klang-compiler/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/crochethk/klang-compiler.svg?style=for-the-badge
[forks-url]: https://github.com/crochethk/klang-compiler/network/members
[stars-shield]: https://img.shields.io/github/stars/crochethk/klang-compiler.svg?style=for-the-badge
[stars-url]: https://github.com/crochethk/klang-compiler/stargazers
[issues-shield]: https://img.shields.io/github/issues/crochethk/klang-compiler.svg?style=for-the-badge
[issues-url]: https://github.com/crochethk/klang-compiler/issues
[license-shield]: https://img.shields.io/github/license/crochethk/klang-compiler.svg?style=for-the-badge
[license-url]: https://github.com/crochethk/klang-compiler/blob/master/LICENSE.txt
[product-screenshot]: images/code-screenshot.png
[C-badge]: https://img.shields.io/badge/C-00599C?logo=c&logoColor=white
[Java]: https://img.shields.io/badge/JDK-%23ED8B00.svg?logo=openjdk&logoColor=white
[Java-url]: https://openjdk.org/
[Antlr4]: https://img.shields.io/badge/Antlr_4-0
[Antlr-url]: https://www.antlr.org
