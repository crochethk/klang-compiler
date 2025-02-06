<!-- This readme was created based on https://github.com/othneildrew/Best-README-Template -->
<a id="readme-top"></a>

<h3 align="center">The Klang Compiler</h3>

  <p align="center">
    A compiler for the Klang programming language.
  </p>
</div>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <a href="#how-it-works">How it works</a>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#quick-start">Quick Start</a></li>
        <li><a href="#use-selfcontained-jar">Selfcontained JAR</a></li>
      </ul>
    </li>
  </ol>
</details>


<!-- ABOUT THE PROJECT -->
## About The Project

This project is a compiler for the "Klang" programming language, developed as 
part of a Compiler Construction course.
The compiler's main focus is to produce GNU Assembly Code from a given Klang 
source code file. It also is capable of generating Java Byte Code (JBC) – though
the supported language features are limited at the moment. 

The language's syntax is slightly inspired by [Rust](https://www.rust-lang.org) 
and a program may look something like the following screenshot:

![Example Klang Program][product-screenshot]

### How it works
The language is defined using ANTLR 4 grammar which is then used to generate
an according Lexer and Parser. The latter creates a Parse Tree which is
then transformed into a Visitor Pattern based Abstract Syntax Tree (AST). It 
is then traversed by several visitors, each performing its respective task(s).
In summary there is a PrettyPrinter, a TypeChecker and several Code Generator
visitors.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- GETTING STARTED -->
## Getting Started

### Prerequisites
* [![Java][Java]][Java-url] ≥23
* `gcc` to compile generated helpers and link with assembly code
* (optional) WSL or linux if you want to use the provided bash scripts

### Quick Start
The easiest way to start compiling Klang files is to use the provided bash
script `scripts/compile_kang.sh`.
To demonstrate this the following steps will show how to compile the file
`examples/point.k`.

1. If not done yet, get a local copy of this repo
    ```sh
    git clone https://github.com/crochethk/klang-compiler.git
    cd klang-compiler
    ```

2. Run the script
    ```sh
    ./scripts/compile_kang.sh asm "./out" "examples/point.k"
    ```
    This will create a subdirectory `./out` with 3 files:
    - `examples.point.s` - The main assembly code.
    - `examples.point.h` - Header with definitions for integration with C code.
    - `examples.point.c` - Helper C code required for some language features.

3. Link and run the program
    ```
    cd out
    gcc -Wall examples.point.s examples.point.c
    ./a.out
    ```
<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Use Selfcontained `JAR`
You can also package the compiler into a selfcontained `jar` using a provided
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
    - `klangc.jar` - The actual compiler exectuable.
    - `klangc.sh` - The convenience script to run the `jar`.

3. Compile a Klang file

    To compile the example source code `examples/point.k` follow these steps:
    - Run
        ```sh
        .build/klangc.sh examples/point.k
        ```
The output and the other steps are similar to [Quick Start](#quick-start).

To print further usage info or inspect all available options run
```sh
./build/klang.sh --help
```

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
