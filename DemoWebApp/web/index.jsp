<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>WebParking</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-KK94CHFLLe+nY2dmCWGMq91rCGa5gtU4mk92HdvYe+M/SXH301p5ILy+dN9+nJOZ" crossorigin="anonymous">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">
        <script src="https://unpkg.com/vue@next"></script>
    </head>
    <body>
        <%@include file="WEB-INF/jspf/header.jspf" %>
        <div id="app" class="container-fluid m-2">
            <div v-if="shared.session">
                <div v-if="error" class="alert alert-danger m-2" role="alert">
                    {{error}}
                </div>
                <div v-else>
                    <h2>Parking</h2>
                    <div class="input-group mb-3">
                        <span class="input-group-text" id="basic-addon1"><i class="bi bi-car-front-fill"></i></span>
                        <input type="text" class="form-control" v-model="newPlate" placeholder="Plate">
                        <input type="text" class="form-control" v-model="newModel" placeholder="Model">
                        <input type="text" class="form-control" v-model="newColor" placeholder="Color">
                        <button class="btn btn-primary" type="button" @click="addStay"><i class="bi bi-box-arrow-in-down"></i></button>
                    </div>
                    <table class="table">
                        <tr>
                            <th>PLACA</th>
                            <th>MODEL</th>
                            <th>COLOR</th>
                            <th>BEGIN</th>
                            <th class="text-end">PRICE</th>
                            <th>EXIT</th>
                        </tr>
                        <tr v-for="item in list" :key="item.vehiclePlate">
                            <td>{{ item.vehiclePlate }}</td>
                            <td>{{ item.vehicleModel }}</td>
                            <td>{{ item.vehicleColor }}</td>
                            <td>{{ item.beginStay }}</td>
                            <td class="text-end">
                                {{ getPrice(item.beginStay).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) }}
                            </td>
                            <td>
                                <button type="button" @click="finishStay(item.rowId)" class="btn btn-success btn-sm">
                                    <i class="bi bi-box-arrow-up"></i> Exit
                                </button>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </div>
        <script>
            const app = Vue.createApp({
                data() {
                    return {
                        shared: shared,
                        error: null,
                        now: new Date(),
                        newModel: '', 
                        newColor: '', 
                        newPlate: '',
                        hourPrice: 0.0,
                        list: [],
                    }
                },
                methods: {
                    async request(url = "", method, data) {
                        try{
                            const response = await fetch(url, {
                                method: method,
                                headers: {"Content-Type": "application/json"},
                                body: JSON.stringify(data)
                            });
                            if(response.status==200){
                                return response.json();
                            }else{
                                this.error = response.statusText;
                            }
                        } catch(e){
                            this.error = e;
                            return null;
                        }
                    },
                    async loadList() {
                        const data = await this.request("/DemoWebApp/api/parking", "GET");
                        if(data) {
                            this.hourPrice = data.hourPrice;
                            this.list = data.list;
                        }
                    },
                    async addStay() {
                        const data = await this.request("/DemoWebApp/api/parking", "POST", {plate: this.newPlate, model: this.newModel, color: this.newColor});
                        if(data) {
                            this.newPlate = ''; 
                            this.newModel = ''; 
                            this.newColor = '';
                            await this.loadList();
                        }
                    },
                    async finishStay(rowId) {
                        const data = await this.request("/DemoWebApp/api/parking", "PUT", {id: rowId});
                        if(data) {
                            await this.loadList();
                        }
                    },
                    getPrice(beginStay) {
                        let begin = new Date(Date.parse(beginStay));
                        let ms = this.now - begin;
                        return this.hourPrice * ms / 60 / 60 / 1000;
                    }
                },
                mounted() {
                    this.loadList();
                    setInterval(() => {this.now = new Date();}, 1000);
                }
            });
            app.mount('#app');
        </script>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha3/dist/js/bootstrap.bundle.min.js" integrity="sha384-ENjdO4Dr2bkBIFxQpeoTz1HIcje39Wm4jDKdf19U8gI4ddQ3GYNS7NTKfAdVQSZe" crossorigin="anonymous"></script>
    </body>
</html>