import {
  AfterViewInit,
  Component,
  ElementRef,
  forwardRef,
  Input,
  OnDestroy,
  ViewChild,
  ViewEncapsulation,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { sql } from '@codemirror/lang-sql';
import { EditorState } from '@codemirror/state';
import {
  EditorView,
  highlightActiveLine,
  highlightActiveLineGutter,
  keymap,
  lineNumbers,
} from '@codemirror/view';
import { defaultKeymap, history, historyKeymap } from '@codemirror/commands';
import { bracketMatching, indentOnInput, syntaxHighlighting, defaultHighlightStyle } from '@codemirror/language';

@Component({
  selector: 'app-sql-editor',
  standalone: true,
  template: `<div class="sql-editor-host" #host></div>`,
  styles: [
    `
      :host {
        display: block;
        width: 100%;
      }
      .sql-editor-host {
        border: 1px solid var(--border);
        border-radius: var(--radius-md);
        overflow: hidden;
        min-height: 180px;
      }
      .sql-editor-host :global(.cm-editor) {
        min-height: inherit;
        font-size: 13px;
        font-family: 'Cascadia Code', 'Fira Code', Consolas, monospace;
      }
      .sql-editor-host :global(.cm-scroller) {
        min-height: 180px;
      }
      .sql-editor-host :global(.cm-gutters) {
        background: #181825;
        border-right: 1px solid #313244;
        color: #6c7086;
      }
      .sql-editor-host :global(.cm-activeLineGutter) {
        background: #313244;
      }
      .sql-editor-host :global(.cm-activeLine) {
        background: rgba(137, 180, 250, 0.06);
      }
    `,
  ],
  encapsulation: ViewEncapsulation.None,
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SqlEditorComponent),
      multi: true,
    },
  ],
})
export class SqlEditorComponent implements ControlValueAccessor, AfterViewInit, OnDestroy {
  @ViewChild('host', { static: true }) hostRef!: ElementRef<HTMLDivElement>;

  @Input() minHeight = '220px';

  private view?: EditorView;
  private innerValue = '';
  private disabled = false;
  private onChange: (value: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;
  private viewReady = false;

  ngAfterViewInit(): void {
    this.hostRef.nativeElement.style.minHeight = this.minHeight;
    this.initEditor();
    this.viewReady = true;
    if (this.innerValue) {
      this.setEditorContent(this.innerValue);
    }
    if (this.disabled) {
      this.view?.contentDOM.setAttribute('contenteditable', 'false');
    }
  }

  ngOnDestroy(): void {
    this.view?.destroy();
  }

  writeValue(value: string | null): void {
    this.innerValue = value ?? '';
    if (this.viewReady) {
      this.setEditorContent(this.innerValue);
    }
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
    if (this.view) {
      this.view.contentDOM.setAttribute('contenteditable', isDisabled ? 'false' : 'true');
    }
  }

  private initEditor(): void {
    this.view = new EditorView({
      state: EditorState.create({
        doc: this.innerValue,
        extensions: [
          lineNumbers(),
          highlightActiveLineGutter(),
          highlightActiveLine(),
          history(),
          indentOnInput(),
          bracketMatching(),
          syntaxHighlighting(defaultHighlightStyle, { fallback: true }),
          sql(),
          keymap.of([...defaultKeymap, ...historyKeymap]),
          EditorView.lineWrapping,
          EditorView.updateListener.of((update) => {
            if (update.docChanged) {
              const value = update.state.doc.toString();
              this.innerValue = value;
              this.onChange(value);
            }
            if (update.focusChanged && update.view.hasFocus) {
              this.onTouched();
            }
          }),
          EditorView.theme({
            '&': { height: '100%' },
            '.cm-content': { padding: '12px 0' },
            '.cm-line': { padding: '0 8px' },
          }),
        ],
      }),
      parent: this.hostRef.nativeElement,
    });
  }

  private setEditorContent(value: string): void {
    if (!this.view) {
      return;
    }
    const current = this.view.state.doc.toString();
    if (current === value) {
      return;
    }
    this.view.dispatch({
      changes: { from: 0, to: this.view.state.doc.length, insert: value },
    });
  }
}
