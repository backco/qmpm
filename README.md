# qmpm - quality metrics for process models

**qmpm** is a cross-paradigm evaluation framework for comparing process models against event logs. Currently, only XES log files are supported, but CSV supported is on the way. The evaluation metrics implemented are model-agnostic and based on the underlying state-transition system, allowing for comparison of models across modelling paradigms, e.g. declarative vs. imperative. For precision and generalization metrics, the model must be perfectly fitting, meaning alignment may need to be applied prior to using qmpm.

## Usage

Download qmpm.jar. Run from the command line using:

`java -jar qmpm.jar [OPTIONS]...[FILE/PATH]...`

For a help text describing all available options, simply run: `java -jar qmpm.jar -h`.

Options taking arguments should have a space between each argument. For example, the following command will run MINERful on log.xes with a confidence threshold of 0.6 and an interest factor threshold of 0.4, subsequently compute the mined model's precision, generalization (event-based), and generalization (state-based), and show progress underway:

`java -jar qmpm.jar -sPgG -M 0.6 0.4 "log.xes"`

If the path to a directory is given as input, qmpm will search for all relevant files in that directory and its subdirectories.

## Metrics
#### Supported
- Precision
- Generalization (event-based)
- Generalization (state-based)
- Cross validation

The first 3 metrics are implemented as described in: *"Replaying history on process models for conformance checking and performance analysis"*, van der Aalst, et al. 2012.

## Model formats
#### Supported
- DCR Graphs
- Declare Maps
- MINERful's Declare Map variant 'ProcessModel'
- Petri nets
#### Upcoming
- Process trees
- Hybrid models

Pre-built models can be loaded from `.pnml` (Petri net) and `.decl` (Declare) files

The following miners are built in to qmpm:

## Miners
#### Supported
- Inductive Miner (IM)
- MINERful
- MINERful w/ automatic parameter tuning
- ParNek Miner
#### Upcoming
- Declare Maps Miner
- Others
