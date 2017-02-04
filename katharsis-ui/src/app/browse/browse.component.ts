import * as _ from 'lodash';

import {Component} from "@angular/core";

import {TreeTableModule, TreeNode, SharedModule} from 'primeng/primeng';
import {Http} from "@angular/http";
import {LocalStorageService} from 'angular-2-local-storage';

@Component({
    selector: 'browse',  // <home></home>
    templateUrl: 'browse.component.html'
})
export class BrowseComponent {

    public resultNodes: Array<TreeNode>;

    public nodes: Array<TreeNode> = null;

    public query: any = {
        type: "",
        id: "",
        relationship: "",
        parameters: ""
    }


    public lastSearchQuery: Object = {};

    public _baseUrl;

    public types: Array<string> = []

    private _queryTerm: string = "";


    constructor(private http: Http, private localStorageService: LocalStorageService) {
        this._baseUrl = this.localStorageService.get("baseUrl");
        this.query.type = this.localStorageService.get("lastType");

        this.get();
    }

    public get queryTerm() {
        return this._queryTerm;
    }

    public set queryTerm(value: string) {
        this._queryTerm = value;
        this.search();
    }

    public get baseUrl() {
        return this._baseUrl;
    }

    public set baseUrl(value: string) {
        this._baseUrl = value;
        this.localStorageService.set("baseUrl", value);
    }

    public get(newQuery?: any) {
        if (!_.isEmpty(this.baseUrl) && !_.isEmpty(this.query.type)) {

            this.localStorageService.set("lastType", this.query.type);

            if (newQuery) {
                this.query = _.cloneDeep(newQuery);
            }
            let url = this.getUrl();
            this.http.get(url).subscribe(it => {
                let document = it.json();
                this.resultNodes = this.toNodes(document);
                this.search();
            }, error => {
                // TODO error handling
                console.log(error);
            });
            return false;
        } else {
            this.resultNodes = null;
            this.nodes = null;
        }
    }

    public getUrl(): string {
        return _.join([this.normalize(this.baseUrl, '/'), this.normalize(this.query.type, '/'),
            this.normalize(this.query.id, '/'), this.normalize(this.query.relationship, '/'),
            this.normalize(this.query.parameters, '?')], '');
    }

    private normalize(value: string, separator: string): string {
        if (_.isEmpty(value))
            return '';
        if (value.endsWith(separator))
            return value;
        return value + separator;
    }

    private stripSeparators(value: string): string {
        if (value.startsWith('/'))
            value = value.substring(1, value.length);
        if (value.endsWith('/'))
            value = value.substring(0, value.length - 1);
        return value;
    }

    public search() {
        this.nodes = [];
        for (let node of this.resultNodes) {
            let searchedNode = this.searchNode(node);
            if (searchedNode) {
                this.nodes.push(searchedNode);
            }
        }
    }

    private searchNode(node: TreeNode): TreeNode {
        let visibleChildren = [];
        if (node.children) {
            for (let child of node.children) {
                let searchedChild = this.searchNode(child);
                if (searchedChild) {
                    visibleChildren.push(searchedChild);
                }
            }
        }

        let visible = visibleChildren.length > 0
            || node.data.key.indexOf(this.queryTerm) != -1
            || !_.isEmpty(node.data.value) && node.data.value.toString().indexOf(this.queryTerm) != -1;

        if (visible) {
            let clone = _.clone(node);
            clone.children = visibleChildren;
            return clone;
        } else {
            return null;
        }
    }

    private toNodes(object: any): Array<TreeNode> {
        let nodes: Array<TreeNode> = [];
        for (let key in object) {
            if (key == 'resultIds' || key == 'loading' || key == 'query') {
                continue; // TODO
            }

            if (object.hasOwnProperty(key)) {
                let value = object[key];
                let data: any = {
                    key: key
                };
                let children = [];
                if (_.isArray(value)) {
                    for (let index in (value as Array<any>)) {
                        let element = value[index];
                        let child: TreeNode = {
                            data: {
                                key: index
                            },
                            expanded: true,
                            children: this.toNodes(element)
                        };
                        children.push(child);
                    }
                } else if (_.isObject(value)) {
                    children = this.toNodes(value);
                } else {
                    data.value = value;
                    data.type = 'text';

                    if(key == 'id' && object.type){
                        data.type = 'query';
                        data.query =  {
                            type: object.type,
                            id: object.id,
                        };
                    }else if(key == 'type' && object.id){
                        data.type = 'query';
                        data.query =  {
                            type: object.type
                        };
                    }else if (!_.isEmpty(value) && _.isString(value) && (value.startsWith("http://") || value.startsWith("https://"))) {

                        let query: any = {
                            type: null,
                            id: "",
                            relationship: "",
                            parameters: ""
                        };

                        if (value.toString().startsWith(this.baseUrl)) {
                            data.type = 'query';
                            data.value = value.substring(this.baseUrl.length);

                            let path = value.substring(this.baseUrl.length);
                            let paramSep = path.indexOf("?");

                            query.parameters = paramSep == -1 ? '' : path.substring(paramSep + 1);
                            path = paramSep == -1 ? path : path.substring(0, paramSep);
                            path = this.stripSeparators(path);

                            for (let someType of this.types) {
                                if (path.startsWith(someType)) {
                                    query.type = someType;
                                    path = path.substring(query.type.length + 1);
                                    break;
                                }
                            }

                            if (query.type) {
                                let pathElements = path.split("/");
                                if (pathElements.length >= 1) {
                                    query.id = pathElements[0];
                                }
                                if (pathElements.length >= 2) {
                                    query.id = pathElements[0];
                                }
                                if (pathElements.length >= 3) {
                                    query.type = null; // cannot handle this
                                }
                            }
                        }

                        if (query.type) {
                            data.type = 'query';
                            data.query = query;
                        } else {
                            data.type = 'url';
                            data.urlValue = value;
                        }

                    }
                }
                let node: TreeNode = {
                    expanded: true,
                    data: data,
                    children: children
                };
                nodes.push(node);
            }
        }

        return nodes;
    }

    ngOnInit() {
        // this.title.getData().subscribe(data => this.data = data);
    }
}
