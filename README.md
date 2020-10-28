# CITTI

## Demo Video
- Official Demo Video
[![DEMO Video Link](https://i.imgur.com/xcV3WAH.png)](https://youtu.be/RssWuITBI1A)

- Prototype Video (the video of the app presented in this github page)
[![Prototype Video Link](https://i.imgur.com/8G87bt9.png)](https://youtu.be/fKPKyPXN_no)

## About CITTI Android App

### Overview
This image shows how we implement all modules in CITTI for Android.
![](https://i.imgur.com/NboIY19.png)
- The cellular information can be collected through calling the [`getAllCellInfo()`](https://developer.android.com/reference/android/telephony/TelephonyManager#getAllCellInfo()) function defined in `android.telephony.TelephonyManager`.
- Feature Generator first maps the collected cellular information to a GPS location. The location can be derived either from searching in a local database implemented with [`Room`](https://developer.android.com/jetpack/androidx/releases/room) in this project, or looking up through open APIs (e.g. Google API, OpenCellid) when the searching in a local database fails. After the GPS location is retrieved, CITTI app further maps the location to required GIS information with the help of [GADM](https://gadm.org/data.html) and [Android ArcGis Runtime SDK](https://developers.arcgis.com/android/latest/).
- Plotter utilizes the features generated from the Feature Generator and plots the corresponding figure using [Androidplot](http://androidplot.com/) library.
- Identifier takes in the figure output from the Plotter, and the CNN model in the Identifier gives out the identification result of that figure. The CNN model is original implemented with [Keras](https://keras.io/) and trained in a PC as described in [About Identifier (CNN) of CITTI](#about-identifier-cnn-of-citti). After the model is well-trained, the Keras model is transformed into a [Tensorflow Lite](https://www.tensorflow.org/lite) model to be deployable for smartphones.

### Experiment Environment
The following table lists the smartphones where we deployed CITTI for our experiments.
<table>
    <tbody>
        <tr>
            <td></td>
            <td>Chipset</td>
            <td>CPU</td>
            <td>GPU</td>
            <td>RAM</td>
            <td>Android Ver.</td>
        </tr>
        <tr>
            <td>Oppo A5</td>
            <td>Qualcomm SDM665 Snapdragon 665 (11 nm)</td>
            <td>Octa-core (4x2.0 GHz Kryo 260 Gold & 4x1.8 GHz Kryo 260 Silver)</td>
            <td>Adreno 610</td>
            <td>4 GB</td>
            <td>10</td>
        </tr>
        <tr>
            <td>ASUS ZenFone Max Pro (ZB602KL)</td>
            <td>Qualcomm SDM636 Snapdragon 636 (14 nm)</td>
            <td>Octa-core (4x1.8 GHz Kryo 260 Gold & 4x1.6 GHz Kryo 260 Silver)</td>
            <td>Adreno 509</td>
            <td>3 GB</td>
            <td>9</td>
        </tr>
        <tr>
            <td>Samsung Galaxy A30s</td>
            <td>Exynos 7904 (14 nm)</td>
            <td>Octa-core (2x1.8 GHz Cortex-A73 & 6x1.6 GHz Cortex-A53)</td>
            <td>Mali-G71 MP2</td>
            <td>4 GB</td>
            <td>9</td>
        </tr>
    </tbody>
</table>


### Instruction
- Source codes of the CITTI Android app are in 📁 `Android`
- All the files in `CITTI/Android/CITTI/app/src/main/assets/` should be put in the directory `/data/data/com.example.CITTI/files/` in the smartphone before launching the CITTI app.
- Require location-related permission.
- `Init` button: read `celltable.csv` into local Android Room Database
- `Query` button: for logging (only for experimental usage)
- `Clear` button: clear the local Android Room Database
- `Test` button: start running CITTI algorithm

※ Notice: The code presented in this github page is only for demostration. Therefore, the cell table is incomplete and the cellular information is provided by a given file containing some pre-collected cellular information (which is collected on an HSR) instead of a SIM card. This however can be controlled by the `CELL_INFO_SRC = SRC_SIM`/`CELL_INFO_SRC = SRC_FILE` in MainActivity, but please make sure the local cell table contains the information of the base stations your smartphones are connecting to. 

## About Identifier (CNN) of CITTI

### Description
📁 `DeepLearning`<br>
&nbsp;&nbsp;&nbsp;&nbsp;📄 `data_preprocess-phase1.ipynb`, 📄 `data_preprocess-phase2.ipynb`: preprocess the original data and generate cellular images<br>
&nbsp;&nbsp;&nbsp;&nbsp;📄 `train.ipynb`: train a CNN from cellular images<br>
&nbsp;&nbsp;&nbsp;&nbsp;📄 `oldTrain.csv`: original training data<br>
&nbsp;&nbsp;&nbsp;&nbsp;📄 `oldTest.csv`, 📄 `new_test_v2.csv`: original two testing data<br>
        

### CNN Model Architecture

```
_________________________________________________________________
Layer (type)                 Output Shape              Param #   
=================================================================
conv2d_1 (Conv2D)            (None, 128, 128, 32)      2432      
_________________________________________________________________
conv2d_2 (Conv2D)            (None, 128, 128, 32)      25632     
_________________________________________________________________
max_pooling2d_1 (MaxPooling2 (None, 64, 64, 32)        0         
_________________________________________________________________
dropout_1 (Dropout)          (None, 64, 64, 32)        0         
_________________________________________________________________
conv2d_3 (Conv2D)            (None, 64, 64, 64)        18496     
_________________________________________________________________
conv2d_4 (Conv2D)            (None, 64, 64, 64)        36928     
_________________________________________________________________
max_pooling2d_2 (MaxPooling2 (None, 32, 32, 64)        0         
_________________________________________________________________
dropout_2 (Dropout)          (None, 32, 32, 64)        0         
_________________________________________________________________
flatten_1 (Flatten)          (None, 65536)             0         
_________________________________________________________________
dense_1 (Dense)              (None, 256)               16777472  
_________________________________________________________________
dropout_3 (Dropout)          (None, 256)               0         
_________________________________________________________________
dense_2 (Dense)              (None, 2)                 514       
=================================================================
Total params: 16,861,474
Trainable params: 16,861,474
Non-trainable params: 0
_________________________________________________________________
```

### Hyperparameters

<table>
    <tbody>
        <tr>
            <td><b>Optimizer</b></td>
            <td>type</td>
            <td>RMSprop</td>
        </tr>
        <tr>
            <td></td>
            <td>lr</td>
            <td>0.0001</td>
        </tr>
        <tr>
            <td></td>
            <td>rho</td>
            <td>0.9</td>
        </tr>
        <tr>
            <td></td>
            <td>epilon</td>
            <td>1e-08</td>
        </tr>
        <tr>
            <td><b>EarlyStopping</b></td>
            <td>monitor</td>
            <td>val_acc</td>
        </tr>
        <tr>
            <td></td>
            <td>patience</td>
            <td>10</td>
        </tr>
        <tr>
            <td><b>ReduceLROnPlateau</b></td>
            <td>monitor</td>
            <td>val_acc</td>
        </tr>
        <tr>
            <td></td>
            <td>patience</td>
            <td>3</td>
        </tr>
        <tr>
            <td></td>
            <td>factor</td>
            <td>0.8</td>
        </tr>
        <tr>
            <td></td>
            <td>min_lr</td>
            <td>0.00001</td>
        </tr>
        <tr>
            <td><b>Batch</b></td>
            <td>size</td>
            <td>128</td>
        </tr>
    </tbody>
</table>

### Experiment Environment
**ASUS E500 G5 Workstation**
<table>
    <tbody>
        <tr>
            <td>OS</td>
            <td>Ubuntu 18.04.3 LTS</td>
        </tr>
        <tr>
            <td>CPU</td>
            <td>Intel(R) Core(TM) i7-8700 CPU @ 3.20GHz</td>
        </tr>
        <tr>
            <td>Core Number</td>
            <td>12</td>
        </tr>
        <tr>
            <td>Memory</td>
            <td>16 GB</td>
        </tr>
        <tr>
            <td>GPU</td>
            <td>GeForce GTX 1080</td>
        </tr>
    </tbody>
</table>

### Training Time
The training process based on the above model architecture and hyperparameters settings generally early-stops within epoch 15 and 25. Each epoch takes about 150 seconds, so the training time of a CITTI CNN model is about 40 minutes to one hour.

The following is 5 example epochs reported by Keras framework during our training process:
```
Epoch 1/40
819/819 [==============================] - 160s 196ms/step - loss: 0.0891 - acc: 0.9641 - val_loss: 0.0539 - val_acc: 0.9865

Epoch 00001: val_acc improved from -inf to 0.98648, saving model to ./cnn_models/cnn_0.986_batch128_seg30_step1_SHL
Epoch 2/40
819/819 [==============================] - 153s 187ms/step - loss: 0.0122 - acc: 0.9960 - val_loss: 0.0635 - val_acc: 0.9868

Epoch 00002: val_acc improved from 0.98648 to 0.98676, saving model to ./cnn_models/cnn_0.987_batch128_seg30_step1_SHL
Epoch 3/40
819/819 [==============================] - 153s 187ms/step - loss: 0.0093 - acc: 0.9972 - val_loss: 0.0284 - val_acc: 0.9932

Epoch 00003: val_acc improved from 0.98676 to 0.99316, saving model to ./cnn_models/cnn_0.993_batch128_seg30_step1_SHL
Epoch 4/40
819/819 [==============================] - 153s 187ms/step - loss: 0.0074 - acc: 0.9976 - val_loss: 0.0696 - val_acc: 0.9865

Epoch 00004: val_acc did not improve from 0.99316
Epoch 5/40
528/819 [==================>...........] - ETA: 44s - loss: 0.0059 - acc: 0.9983
```

## Open Dataset Download
[SensingGO](https://sensinggo.org/)
