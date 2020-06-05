# TODO:
# - Check if we need data from outside CSV file?



from collections import defaultdict
import os
import dash
import dash_core_components as dcc
import dash_html_components as html
from dash.dependencies import Input, Output

import pandas as pd

external_stylesheets = ['https://codepen.io/chriddyp/pen/bWLwgP.css']

app = dash.Dash(__name__, external_stylesheets=external_stylesheets)

# df = pd.read_csv('https://plotly.github.io/datasets/country_indicators.csv')

# available_indicators = df['Indicator Name'].unique()


# Fetch all benchmarks
df = pd.read_csv('results.out')

benchmarks = []
results = defaultdict(dict)
solvers = df['solver'].unique()
for i, row in df.iterrows():
    bm = os.path.basename(row['benchmark'])
    benchmarks.append(bm)
    solver = row['solver']
    time = row['time']
    results[bm][solver] = time

x = []
y = []

def benchmarkstr(bm):
    fstr = bm + "\n"
    for s in results[bm]:
        fstr = fstr + s + ": " + str(results[bm][s]) + "\n"
    return fstr


data = defaultdict(dict)
for s in solvers:
    data[s] = []
for k in results:
    for s in solvers:
        data[s].append(results[k][s])


available_solvers = df['solver'].unique()

app.layout = html.Div([
    html.Div([

        html.Div([
            dcc.Dropdown(
                id='xaxis-column',
                options=[{'label': i, 'value': i} for i in available_solvers],
                value=available_solvers[0]
            )
        ],
        style={'width': '48%', 'display': 'inline-block'}),

        html.Div([
            dcc.Dropdown(
                id='yaxis-column',
                options=[{'label': i, 'value': i} for i in available_solvers],
                value=available_solvers[1]
            )
        ],style={'width': '48%', 'float': 'right', 'display': 'inline-block'})
    ]),

    dcc.Graph(id='indicator-graphic'),


        html.Div([
            dcc.Markdown("Benchmark Data (click point)"),
            html.Pre(id='click-data'),
        ], className='three columns')

]
)

@app.callback(
    Output('indicator-graphic', 'figure'),
    [Input('xaxis-column', 'value'),
     Input('yaxis-column', 'value')])
def update_graph(xaxis_column_name, yaxis_column_name):
    xx = data[xaxis_column_name]
    yy = data[yaxis_column_name]
    return {
        'data': [dict(
            x=xx,
            y=yy,
            text=benchmarks,
            mode='markers',
            marker={
                'size': 15,
                'opacity': 0.5,
                'line': {'width': 0.5, 'color': 'white'}
            }
        )],
        'layout': dict(
            xaxis={
                'title': xaxis_column_name,
                'type': 'linear' 
            },
            yaxis={
                'title': yaxis_column_name,
                'type': 'linear' 
            },
            margin={'l': 40, 'b': 40, 't': 10, 'r': 0},
            hovermode='closest'
        )
    }




@app.callback(
    Output('click-data', 'children'),
    [Input('indicator-graphic', 'clickData')])
def display_click_data(clickData):
    if (clickData == None):
        print("Init")
    else:
        return benchmarkstr(clickData['points'][0]['text'])
        


if __name__ == '__main__':
    app.run_server(debug=True)
