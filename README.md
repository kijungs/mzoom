M-Zoom / M-Biz
========================
**M-Zoom (Multidimensional Zoom)** and **M-Biz (Multidimensional Bi-directional Zoom)** are algorithms for detecting dense blocks in tensors. 
They have the following properties: 
 * *scalable*: scales almost linearly with all input factors
 * *provably accurate*: provides high accuracy in real data as well as theoretical guarantees
 * *flexible*: supports high-order tensors, various density measures, multi-block detection, and size bounds

Datasets
========================
The download links for the datasets used in the paper are [here](http://www.cs.cmu.edu/~kijungs/codes/mzoom/)

Building and Running M-Zoom / M-Biz
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
  booktitle = {Joint European Conference on Machine Learning and Knowledge Discovery in Databases},
  pages     = {264--280},
  year      = {2016},
  url       = {http://dx.doi.org/10.1007/978-3-319-46128-1_17},
  doi       = {10.1007/978-3-319-46128-1_17},
  publisher = {Springer}
}

@inproceedings{shin2018mbiz,
  author    = {Kijung Shin and Bryan Hooi and Christos Faloutsos},
  title     = {Fast, Accurate and Flexible Algorithms for Dense Sub-Tensor Mining},
  journal   = {ACM Transactions on Knowledge Discovery from Data (TKDD)},
  volume    = {PP},
  number    = {PP},
  pages     = {PP},
  year      = {PP},
  publisher = {ACM}
}

```
