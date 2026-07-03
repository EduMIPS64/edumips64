import js from "@eslint/js";
import eslintReact from "@eslint-react/eslint-plugin";
import prettierConfig from "eslint-config-prettier";
import prettierPlugin from "eslint-plugin-prettier";
import globals from "globals";
import tseslint from "typescript-eslint";

export default [
  // ESLint built-in recommended rules
  js.configs.recommended,
  // React + hooks rules (ESLint 10 compatible, replaces eslint-plugin-react and eslint-plugin-react-hooks)
  eslintReact.configs.recommended,
  // Project-specific overrides for webapp source files
  {
    files: ["src/webapp/**/*.{js,jsx}"],
    plugins: {
      prettier: prettierPlugin,
    },
    languageOptions: {
      ecmaVersion: "latest",
      sourceType: "module",
      globals: {
        ...globals.browser,
        ...globals.node,
        Atomics: "readonly",
        SharedArrayBuffer: "readonly",
      },
      parserOptions: {
        ecmaFeatures: {
          jsx: true,
        },
      },
    },
    rules: {
      "prettier/prettier": "error",
    },
  },
  // TypeScript-ESLint recommended (non-type-checked) rules for .ts files only.
  // Does not change linting of .js/.jsx files.
  ...tseslint.configs.recommended.map((cfg) => ({
    ...cfg,
    files: ["**/*.ts", "**/*.tsx"],
  })),
  // Disable ESLint rules that conflict with Prettier formatting (must be last)
  prettierConfig,
];
