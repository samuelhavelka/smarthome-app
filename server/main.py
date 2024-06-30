# -*- coding: utf-8 -*-
"""
Created on Thu Jan 28 00:44:25 2021

@author: Samuel Havelka
"""
import cv2
import numpy as np
import os
import tensorflow as tf
from frameextractor import frameExtractor
from handshape_feature_extractor import HandShapeFeatureExtractor

# list of gesture name and label
gesture_dict = {"Num0": "0", 
                "Num1": "1",
                "Num2": "2", 
                "Num3": "3",
                "Num4": "4", 
                "Num5": "5",
                "Num6": "6", 
                "Num7": "7",
                "Num8": "8", 
                "Num9": "9",
                "FanDown": "10",
                "FanOff": "11", 
                "FanOn": "12",
                "FanUp": "13",
                "LightOff": "14", 
                "LightOn": "15",
                "SetThermo": "16"
}


def extract_images(source_path, dest_path, return_log=False, frame_ratio=[0.5]):
    # extract middle frame from all videos in source_path
    count=0
    log = {}

    # if destination folder dne, then create folder
    if not os.path.exists(dest_path):
        os.makedirs(dest_path)
    
    for filename in os.listdir(source_path):

        # Extract middle frame and save to \test\00001.png
        video_path = os.path.join(source_path, filename)

        for ratio in frame_ratio:
            frameExtractor(video_path, dest_path, count, ratio)

            # add filename and gesture to log
            if return_log:
                gesture = filename.split("_")[0]
                log[count+1] = gesture_dict[gesture]

            count += 1
    
    return log


def extract_features(source_path):
    # extract feature vector from directory of images

    features = []
    count=0

    for filename in os.listdir(source_path):
        # load image with opencv
        image_path = os.path.join(source_path, filename)
        img = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE)

        # extract feature vector
        features.append(HandShapeFeatureExtractor.extract_feature(HandShapeFeatureExtractor.get_instance(), img))

        count += 1
    
    return features


# compare feature vector of test vidfeo to training features vectors
# lowestt cossimilarity is recognized gesture
def predict(test_feats, train_feats, labels):

    output = []

    # compare feature to all extracted features from training set
    for feature in test_feats:

        similarity = np.argmin(tf.keras.losses.cosine_similarity(feature, train_feats, axis=-1))
        predicted_label = labels[similarity+1]

        output.append(predicted_label)
    
    return output



if __name__ == '__main__':

      # Extract the middle frame of each training gesture video
    extract_images(source_path='test', dest_path='test_imgs')
    train_log = extract_images(source_path='traindata', dest_path='train_imgs', return_log=True, frame_ratio=[0.3,0.5,0.8])

    # Extract features
    test_features = extract_features(source_path='test_imgs')
    train_features = extract_features(source_path='train_imgs')

    # predict labels
    results = predict(test_features, train_features, train_log)

    # save as csv
    np.savetxt("Results.csv", results, delimiter=',', fmt='% s')
    print("DONE!")
