M-Zoom: Fast Dense-Block Detection in Tensors with Quality Guarantees
========================
**M-Zoom (Multidimensional Zoom)** is an algorithm for detecting dense blocks in tensors.
**M-Zoom** has the following properties:
 * *scalable*: scales almost linearly with all input factors
 * *provably accurate*: provides high accuracy in real data as well as theoretical guarantees
 * *flexible*: supports high-order tensors, various density measures, multi-block detection, and size bounds

Datasets
========================
The download links for the datasets used in the paper are [here](http://www.cs.cmu.edu/~kijungs/codes/mzoom/)

Building and Running M-Zoom
========================
Please see [User Guide](user_guide.pdf)

Running Demo
========================
For demo, please type 'make'

Reference
========================
If you use this code as part of any published research, please acknowledge the following paper.
```
@inproceedings{shin2016mzoom,
author    = {Kijung Shin and Bryan Hooi and Christos Faloutsos},
title     = {M-Zoom: Fast Dense-Block Detection in Tensors with Quality Guarantees},
booktitle = {PKDD},
year      = {2016}
}
```
