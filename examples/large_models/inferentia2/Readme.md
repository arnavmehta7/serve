# Large model inference on Inferentia2 

This document briefs on serving large HuggingFace (HF) models on [AWS Inferentia2](https://aws.amazon.com/ec2/instance-types/inf2/), Inf2 instances. We use

Inferentia2 uses [Neuron SDK](https://aws.amazon.com/machine-learning/neuron/) which is build on top of PyTorch XLA stack. For large model inference [`transformers-neuronx`](https://github.com/aws-neuron/transformers-neuronx) package is used that takes care of model partitioning and running inference. 

Lets have a look at the steps to prepare our model for inference on Inf2 instances.

### Step 1: Inf2 instance

Get an Inf2 instance, ssh to it, make sure to use the following DLAMI as it comes with PyTorch and neccessary packages for AWS Neuron SDK pre-installed.
DLAMI Name: ` Deep Learning AMI Neuron PyTorch 1.13.0 (Ubuntu 20.04) 20230226 Amazon Machine Image (AMI)`

### Step 1: Package Installations

Follow the steps below to complete package installations

```bash

# Update Neuron Runtime
sudo apt-get install aws-neuronx-collectives=2.* -y
sudo apt-get install aws-neuronx-runtime-lib=2.* -y

# Activate Python venv 
source /opt/aws_neuron_venv_pytorch/bin/activate 

# Set pip repository pointing to the Neuron repository 
python -m pip config set global.extra-index-url https://pip.repos.neuron.amazonaws.com

# Update Neuron Compiler and Framework
python -m pip install --upgrade neuronx-cc==2.* torch-neuronx torchvision

pip install git+https://github.com/aws-neuron/transformers-neuronx.git transformers -U

```



### Step 2: Save the model splitted checkpoints compatibale with `transformers-neuronx`

```bash
 python save_split_checkpoints.py --model_name facebook/opt-13b --save_path './opt-13b-split'

```


### Step 3: Generate Tar/ MAR file

Navigate up to `large_model/inferentia2` directory.

```bash
torch-model-archiver --model-name opt --version 1.0 --handler inf2_handler.py --extra-files ./opt-13b-split  -r requirements.txt --config-file model-config.yaml --archive-format tgz

```

### Step 4: Add the mar file to model store

```bash
mkdir model_store
mv opt.tar.gz model_store
```

### Step 5: Start torchserve

Update config.properties and start torchserve

```bash
torchserve --ncs --start --model-store model_store --models opt.tar.gz
```

### Step 6: Run inference

```bash
curl -v "http://localhost:8080/predictions/opt" -T sample_text.txt
```