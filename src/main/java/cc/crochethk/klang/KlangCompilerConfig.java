package cc.crochethk.klang;

record KlangCompilerConfig(
        String outputDir,
        boolean showParseTree,
        boolean noCompile,
        boolean buildAst,
        boolean prettyPrintAst,
        boolean typeCheck,
        boolean generateJbc,
        boolean generateAsm) {
    public static class KlangCompilerConfigBuilder {
        private String outputDir;
        private boolean showParseTree;
        private boolean noCompile;
        private boolean buildAst;
        private boolean prettyPrintAst;
        private boolean typeCheck;
        private boolean generateJbc;
        private boolean generateAsm;

        public KlangCompilerConfigBuilder outputDir(final String outputDir) {
            this.outputDir = outputDir;
            return this;
        }

        public KlangCompilerConfigBuilder showParseTree(final boolean showParseTree) {
            this.showParseTree = showParseTree;
            return this;
        }

        public KlangCompilerConfigBuilder noCompile(final boolean noCompile) {
            this.noCompile = noCompile;
            return this;
        }

        public KlangCompilerConfigBuilder buildAst(final boolean buildAst) {
            this.buildAst = buildAst;
            return this;
        }

        public KlangCompilerConfigBuilder prettyPrintAst(final boolean prettyPrintAst) {
            this.prettyPrintAst = prettyPrintAst;
            return this;
        }

        public KlangCompilerConfigBuilder typeCheck(final boolean typeCheck) {
            this.typeCheck = typeCheck;
            return this;
        }

        public KlangCompilerConfigBuilder generateJbc(final boolean generateJbc) {
            this.generateJbc = generateJbc;
            return this;
        }

        public KlangCompilerConfigBuilder generateAsm(final boolean generateAsm) {
            this.generateAsm = generateAsm;
            return this;
        }

        public KlangCompilerConfig build() {
            return new KlangCompilerConfig(this.outputDir, this.showParseTree, this.noCompile,
                    this.buildAst, this.prettyPrintAst, this.typeCheck, this.generateJbc, this.generateAsm);
        }

        @Override
        public String toString() {
            return "KlangCompilerConfigBuilder(outputDir=" + this.outputDir + ", showParseTree="
                    + this.showParseTree + ", noCompile=" + this.noCompile + ", buildAst=" + this.buildAst
                    + ", prettyPrintAst=" + this.prettyPrintAst + ", typeCheck=" + this.typeCheck
                    + ", generateJbc=" + this.generateJbc + ", generateAsm=" + this.generateAsm + ")";
        }
    }

    public static KlangCompilerConfigBuilder builder() {
        return new KlangCompilerConfig.KlangCompilerConfigBuilder();
    }
}