import type { UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated';
import MagicString from 'magic-string';

const customConfig: UserConfigFn = (env) => ({
  plugins: [
    {
      name: 'vite-plugin-rewrite-polymer-global',
      transform(code, id) {
        // Workaround esbuild issue with chunked code running in wrong order
        // See https://github.com/vitejs/vite/issues/5142
        if (id.includes('.js') && code.includes('JSCompiler_renameProperty')) {
          const ms = new MagicString(code);
          ms.replaceAll(/JSCompiler_renameProperty\(([^,]+),[^)]+\)/g, '$1');

          return {
            code: ms.toString(),
            map: ms.generateMap({
              file: id,
              includeContent: true,
            }),
          };
        }
      },
    },
  ],
  test: {
    include: ['./tests/**/*.{test,spec}.ts?(x)'],
    globals: true,
    browser: {
      enabled: true,
      name: 'chrome',
    },
  },
});

export default overrideVaadinConfig(customConfig);
