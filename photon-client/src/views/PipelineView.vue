<template>
  <div>
    <camera-and-pipeline-select />
    <v-row>
      <!-- vision tabs -->
      <v-col
        cols="6"
        class="colsClass"
      >
        <v-tabs
          v-if="($store.getters.currentPipelineIndex + 1) !== 0"
          v-model="selectedTab"
          fixed-tabs
          background-color="#212121"
          dark
          height="48"
          slider-color="#4baf62"
        >
          <v-tab>Input</v-tab>
          <v-tab>Threshold</v-tab>
          <v-tab>Contours</v-tab>
          <v-tab>Output</v-tab>
          <v-tab>3D</v-tab>
        </v-tabs>
        <div
          v-else
          style="height: 48px"
        />
        <div style="padding-left:30px">
          <keep-alive>
            <!-- vision component -->
            <component
              :is="selectedComponent"
              ref="component"
              v-model="$store.getters.pipeline"
              @update="$emit('save')"
            />
          </keep-alive>
        </div>
      </v-col>
      <v-col
        cols="6"
        class="colsClass"
      >
        <div>
          <!-- camera image tabs -->
          <v-tabs
            v-if="($store.getters.currentPipelineIndex + 1) !== 0"
            v-model="isBinaryNumber"
            background-color="#212121"
            dark
            height="48"
            slider-color="#4baf62"
            centered
            style="padding-bottom:10px"
            @change="handleInput('isBinary',$store.getters.pipeline.isBinary)"
          >
            <v-tab>Normal</v-tab>
            <v-tab>Threshold</v-tab>
          </v-tabs>
          <div
            v-else
            style="height: 58px"
          />
          <!-- camera image stream -->
          <div class="videoClass">
            <v-row align="center">
              <cvImage
                :address="$store.getters.streamAddress"
                :scale="75"
                @click="onImageClick"
              />
            </v-row>
            <v-row justify="end">
              <span style="margin-right: 45px">FPS:{{ parseFloat(fps).toFixed(2) }}</span>
            </v-row>
            <v-row align="center">
              <v-simple-table
                style="text-align: center;background-color: transparent; display: block;margin: auto"
                dense
                dark
              >
                <template v-slot:default>
                  <thead>
                    <tr>
                      <th class="text-center">
                        Target
                      </th>
                      <th class="text-center">
                        Pitch
                      </th>
                      <th class="text-center">
                        Yaw
                      </th>
                      <th class="text-center">
                        Area
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr
                      v-for="(value, index) in $store.getters.targets"
                      :key="index"
                    >
                      <td>{{ index }}</td>
                      <td>{{ parseFloat(value['pitch']).toFixed(2) }}</td>
                      <td>{{ parseFloat(value['yaw']).toFixed(2) }}</td>
                      <td>{{ parseFloat(value['area']).toFixed(2) }}</td>
                    </tr>
                  </tbody>
                </template>
              </v-simple-table>
            </v-row>
          </div>
        </div>
      </v-col>
    </v-row>
    <!-- snack bar -->
    <v-snackbar
      v-model="snackbar"
      :timeout="3000"
      top
      color="error"
    >
      <span style="color:#000">Can not remove the only pipeline!</span>
      <v-btn
        color="black"
        text
        @click="snackbar = false"
      >
        Close
      </v-btn>
    </v-snackbar>
  </div>
</template>

<script>
    import CameraAndPipelineSelect from "../components/pipeline/CameraAndPipelineSelect";
    import cvImage from '../components/common/cv-image'
    import InputTab from './PipelineViewes/InputTab'
    import ThresholdTab from './PipelineViewes/ThresholdTab'
    import ContoursTab from './PipelineViewes/ContoursTab'
    import OutputTab from './PipelineViewes/OutputTab'
    import pnpTab from './PipelineViewes/3D'

    export default {
        name: 'CameraTab',
        components: {
            CameraAndPipelineSelect,
            cvImage,
            InputTab,
            ThresholdTab,
            ContoursTab,
            OutputTab,
            pnpTab,
        },
        data() {
            return {
                selectedTab: 0,
                snackbar: false,

            }
        },
        computed: {
            isBinaryNumber: {
                get() {
                    return this.$store.getters.pipeline.isBinary ? 1 : 0
                },
                set(value) {
                    this.$store.commit('isBinary', !!value);
                }
            },
            selectedComponent: {
                get() {
                    return (this.$store.getters.currentPipelineIndex + 1) === 0 ? "InputTab" : ["InputTab", "ThresholdTab", "ContoursTab", "OutputTab", "pnpTab"][this.selectedTab];
                }
            },
            fps: {
                get() {
                    return this.$store.state.point.fps;
                }
            }
        },
        methods: {
            onImageClick(event) {
                if (this.selectedTab === 1) {
                    this.$refs.component.onClick(event);
                }
            },
        }
    }
</script>

<style scoped>
    .colsClass {
        padding: 0 !important;
    }

    .videoClass {
        text-align: center;
    }

    th {
        width: 80px;
        text-align: center;
    }

</style>