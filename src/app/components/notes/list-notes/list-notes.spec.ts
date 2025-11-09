import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListNotes } from './list-notes';

describe('ListNotes', () => {
  let component: ListNotes;
  let fixture: ComponentFixture<ListNotes>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListNotes]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListNotes);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
