import {BrowserModule} from '@angular/platform-browser';
import {NgModule, APP_INITIALIZER, Inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpModule} from '@angular/http';
import {RouterModule, Router, NavigationStart} from '@angular/router';

import {StoreModule} from '@ngrx/store';
import {EffectsModule} from '@ngrx/effects';
import {StoreDevtoolsModule} from '@ngrx/store-devtools';
import {routerReducer, RouterStoreModule} from '@ngrx/router-store';

import {AppComponent} from './app.component';
import {BrowseComponent} from './browse';

import {
    InputTextModule, InputTextareaModule, DropdownModule, CalendarModule, DataTableModule,
    AutoCompleteModule, SharedModule, DataGridModule, DataListModule, ButtonModule,
    DataScrollerModule, PaginatorModule, PanelModule, PanelMenuModule, TreeTableModule
} from 'primeng/primeng';

import { LocalStorageModule } from 'angular-2-local-storage';

import {ROUTES} from './app.routes';


@NgModule({
    declarations: [
        AppComponent,
        BrowseComponent,
    ],
    imports: [
        BrowserModule,
        FormsModule,
        HttpModule,
        RouterModule.forRoot(ROUTES, {useHash: true}),

        LocalStorageModule.withConfig({
            prefix: 'my-app',
            storageType: 'localStorage'
        }),

        // primeng
        InputTextModule, InputTextareaModule, DataTableModule, SharedModule, AutoCompleteModule,
        DataGridModule, DataListModule, DataScrollerModule, DataTableModule,
        DropdownModule, ButtonModule,
        PaginatorModule, PanelModule,
        TreeTableModule

    ],
    bootstrap: [
        AppComponent
    ]
})
export class AppModule {

}
