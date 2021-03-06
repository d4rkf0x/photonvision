<template>
  <div>
    <!-- Special hidden upload input that gets 'clicked' when the user selects the right dropdown item' -->
    <input
      ref="file"
      type="file"
      accept=".csv"
      style="display: none;"

      @change="readFile"
    >

    <v-select
      v-model="selectedModel"
      dark
      color="accent"
      item-color="secondary"
      label="Select a target model"
      :items="FRCtargets"
      item-text="name"
      item-value="data"
      @change="onModelSelect"
    />
    <v-divider />
    <CVslider
      v-model="value.accuracy"
      class="pt-2"
      slider-cols="12"
      name="Contour simplification amount"
      :disabled="selectedModel === null"
      min="0"
      max="100"
      @input="handleData('accuracy')"
      @rollback="e => rollback('accuracy', e)"
    />
    <v-divider class="pb-2" />
    <mini-map
      class="miniMapClass"
      :targets="targets"
      :horizontal-f-o-v="horizontalFOV"
    />
    <v-snackbar
      v-model="snack"
      top
      :color="snackbar.color"
    >
      <span>{{ snackbar.text }}</span>
    </v-snackbar>
  </div>
</template>

<script>
    import Papa from 'papaparse';
    import miniMap from '../../components/pipeline/3D/MiniMap';
    import CVslider from '../../components/common/cv-slider'
    import FRCtargetsConfig from '../../assets/FRCtargets'

    export default {
        name: "SolvePNP",
        components: {
            CVslider,
            miniMap
        },
      // eslint-disable-next-line vue/require-prop-types
        props: ['value'],
        data() {
            return {
                selectedModel: null,
                FRCtargets: null,
                snackbar: {
                    color: "success",
                    text: ""
                },
                snack: false
            }
        },
        computed: {
            targets: {
                get() {
                    return "FIXME"; // TODO fix
                }
            },
            horizontalFOV: {
                get() {
                    let index = this.$store.getters.currentPipelineSettings.cameraVideoModeIndex;
                    let FOV = this.$store.getters.currentCameraSettings.fov;
                    let resolution = this.$store.getters.videoFormatList[index];
                    let diagonalView = FOV * (Math.PI / 180);
                    let diagonalAspect = Math.hypot(resolution.width, resolution.height);
                    return Math.atan(Math.tan(diagonalView / 2) * (resolution.width / diagonalAspect)) * 2 * (180 / Math.PI)
                }
            },
            allow3D: {
                get() {
                    return this.$store.getters.currentCameraSettings.calibrated;
                }
            }
        },
        mounted() {
            let tmp = [];
            for (let t in FRCtargetsConfig) {
                if (FRCtargetsConfig.hasOwnProperty(t)) {
                    tmp.push({name: t, data: FRCtargetsConfig[t]})
                }
            }

            // Special dropdown item for uploading your own model
            // data is what gets put in selectedMode, so we add a special field
            tmp.push({name: "Custom model", data: {isCustom: true}});

            this.FRCtargets = tmp;
        },
        methods: {
            readFile(event) {
                let file = event.target.files[0];
                Papa.parse(file, {
                    complete: this.onParse,
                    skipEmptyLines: true
                });
            },
            onModelSelect() {
              if (this.selectedModel.isCustom) {
                this.$refs.file.click();
              } else {
                this.uploadPremade();
              }
            },
            onParse(result) {
                if (result.data.length > 0) {
                    let data = [];
                    for (let i = 0; i < result.data.length; i++) {
                        let item = result.data[i];

                        let tmp = [];
                        tmp.push(Number(item[0]));
                        tmp.push(Number(item[1]));
                        if (isNaN(tmp[0]) || isNaN(tmp[1])) {
                            this.snackbar = {
                                color: "error",
                                text: `Error: custom target CSV contained a non-numeric value on line ${i + 1}`
                            };
                            this.snack = true;

                            this.selectedModel = null;
                            return;
                        }
                        data.push(tmp);
                    }
                    this.uploadModel(data);
                } else {
                    this.snackbar = {
                        color: "error",
                        text: "Error: custom target CSV was empty"
                    };
                    this.snack = true;

                    this.selectedModel = null;
                }
            },
            uploadPremade() {
                this.uploadModel(this.selectedModel, true);
            },
            uploadModel(model, premade = false) {
                this.axios.post("http://" + this.$address + "/api/vision/pnpModel", model).then(() => {
                    this.snackbar = {
                        color: "success",
                        text: premade ? "Target model changed successfully" : "Custom target model uploaded and selected successfully"
                    };
                    this.snack = true;
                }).catch(() => {
                    this.snackbar = {
                        color: "error",
                        text: "An error occurred selecting a target model"
                    };
                    this.snack = true;

                    this.selectedModel = null;
                });
            }
        }
    }
</script>

<style scoped>
    .miniMapClass {
        width: 400px !important;
        height: 100% !important;

        margin-left: auto;
        margin-right: auto;
    }
</style>