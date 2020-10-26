# CITTI

## Demo Video
- Official Demo Video
[![DEMO Video Link](https://i.imgur.com/xcV3WAH.png)](https://youtu.be/RssWuITBI1A)

- Prototype Video (the video of the app presented in this open source)
[![Prototype Video Link](https://i.imgur.com/8G87bt9.png)](https://youtu.be/fKPKyPXN_no)

## CITTI Android App


## About Identifier (CNN) of CITTI

### Description
📁 DeepLearning<br>
&nbsp;&nbsp;&nbsp;&nbsp;📄 data_preprocess-phase1.ipynb, 📄 data_preprocess-phase2.ipynb: preprocess the original data and generate cellular images<br>
&nbsp;&nbsp;&nbsp;&nbsp;📄 train.ipynb.ipynb: train a CNN from cellular images<br>
&nbsp;&nbsp;&nbsp;&nbsp;📄 oldTrain.csv: original training data<br>
&nbsp;&nbsp;&nbsp;&nbsp;📄 oldTest.csv, 📄 new_test_v2.csv: original two testing data<br>
        

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
A PC with a 16-core CPU and an Nvidia GTX 1080 GPU.

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
