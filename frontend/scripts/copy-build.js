import { cp, mkdir, rm } from 'node:fs/promises';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const frontendRoot = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const source = resolve(frontendRoot, 'dist/pos-item-ui/browser');
const staticRoot = resolve(frontendRoot, '../src/main/resources/static');
const destination = resolve(staticRoot, 'jk_wellness');
await rm(destination, { recursive: true, force: true });
await mkdir(staticRoot, { recursive: true });
await mkdir(destination, { recursive: true });
await cp(source, destination, { recursive: true });
console.log(`React build copied to ${destination}.`);