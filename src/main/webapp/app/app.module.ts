import './vendor.ts';

import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { Ng2Webstorage } from 'ngx-webstorage';

import { EquoidSharedModule, UserRouteAccessService } from './shared';
import { EquoidAppRoutingModule} from './app-routing.module';
import { EquoidHomeModule } from './home/home.module';
import { EquoidGraphModule } from './graph/graph.module';
import { EquoidKitchensinkModule } from './kitchensink/kitchensink.module';
import { EquoidAdminModule } from './admin/admin.module';
import { EquoidEntityModule } from './entities/entity.module';
import { customHttpProvider } from './blocks/interceptor/http.provider';
import { PaginationConfig } from './blocks/config/uib-pagination.config';
import { PieDataService } from './piechart/piechart.service'
import { ItemRestDataService } from './graph/item-rest-data.service'
import { JdgFakeDataService } from './graph/jdg-fake-data.service'
// jhipster-needle-angular-add-module-import JHipster will add new module here

import {
    JhiMainComponent,
    NavbarComponent,
    FooterComponent,
    ProfileService,
    PageRibbonComponent,
    ErrorComponent
} from './layouts';

@NgModule({
    imports: [
        BrowserModule,
        EquoidAppRoutingModule,
        Ng2Webstorage.forRoot({ prefix: 'jhi', separator: '-'}),
        EquoidSharedModule,
        EquoidHomeModule,
        EquoidGraphModule,
        EquoidKitchensinkModule,
        EquoidAdminModule,
        EquoidEntityModule,
        // jhipster-needle-angular-add-module JHipster will add new module here
    ],
    declarations: [
        JhiMainComponent,
        NavbarComponent,
        ErrorComponent,
        PageRibbonComponent,
        FooterComponent
    ],
    providers: [
        ProfileService,
        customHttpProvider(),
        PaginationConfig,
        UserRouteAccessService,
        PieDataService,
        ItemRestDataService,
        JdgFakeDataService
    ],
    bootstrap: [ JhiMainComponent ]
})
export class EquoidAppModule {}
